package cengine.lang.mif.semantic

import cengine.console.SysOut
import cengine.lang.mif.MifRadix
import cengine.lang.mif.psi.MifAddressSpec
import cengine.lang.mif.psi.MifContentBlock
import cengine.lang.mif.psi.MifDirective
import cengine.lang.mif.psi.MifNumericValue
import cengine.lang.mif.psi.MifRadixValue
import cengine.util.integer.*
import cengine.util.integer.BigInt.Companion.toBigInt

/**
 * Holds the parsed data from a MIF file.
 *
 * Stores memory content as contiguous sections.
 *
 * @param ADDR The specific unsigned integer type determined for addresses (e.g., UInt8, UInt16).
 * @param WORD The specific integer type determined for data words (e.g., Int16, UInt32).
 * @property addrWidth Metadata about the address type (bit count, signedness).
 * @property width Metadata about the data word type (bit count, signedness).
 * @property depth The number of memory locations specified in the DEPTH directive.
 * @property addressRadix The radix used for addresses in the content block.
 * @property dataRadix The radix used for data values in the content block.
 * @property memory A map where the key is the starting address (of type ADDR) of a contiguous block
 * of initialized memory, and the value is a List<WORD> containing the data
 * values for that block. Addresses not covered are uninitialized.
 */
data class MifData(
    val addrWidth: UnsignedFixedSizeIntNumberT<*>,
    val width: FixedSizeIntNumberT<*>,
    val depth: BigInt = BigInt.ZERO,
    val addressRadix: MifRadix = MifRadix.HEX,
    val dataRadix: MifRadix = MifRadix.HEX,
    val memory: Map<UnsignedFixedSizeIntNumber<*>, List<FixedSizeIntNumber<*>>> = emptyMap(),
) {

    companion object {
        /**
         * Creates a MifData object by analyzing the MIF PSI tree.
         *
         * @param directives A list of parsed MifDirective PSI nodes.
         * @param content The parsed MifContentBlock PSI node (can be null if file is structurally valid but has no CONTENT block).
         * @throws MifSemanticException If required directives are missing, values are invalid,
         * addresses are out of bounds, or other semantic errors occur.
         */
        fun create(directives: List<MifDirective>, content: MifContentBlock?): MifData { // Return MifData<*, *> as types are determined internally

            var widthBI: BigInt? = null
            var depthBI: BigInt? = null
            var addressRadixEnum: MifRadix? = null
            var dataRadixEnum: MifRadix? = null

            // 1. Process Directives
            directives.forEach { directive ->
                try {
                    when (directive) {
                        is MifDirective.Width -> {
                            if (widthBI != null) throw MifSemanticException("Duplicate WIDTH directive found.", directive)
                            widthBI = parsePsiNumericValue(directive.value, MifRadix.DEC) // Width/Depth always base 10
                            if (widthBI <= BigInt.ZERO) throw MifSemanticException("WIDTH must be positive.", directive)
                        }

                        is MifDirective.Depth -> {
                            if (depthBI != null) throw MifSemanticException("Duplicate DEPTH directive found.", directive)
                            depthBI = parsePsiNumericValue(directive.value, MifRadix.DEC)
                            if (depthBI <= BigInt.ZERO) throw MifSemanticException("DEPTH must be positive.", directive)
                        }

                        is MifDirective.AddressRadix -> {
                            if (addressRadixEnum != null) throw MifSemanticException("Duplicate ADDRESS_RADIX directive found.", directive)
                            addressRadixEnum = parsePsiRadixValue(directive.value)
                        }

                        is MifDirective.DataRadix -> {
                            if (dataRadixEnum != null) throw MifSemanticException("Duplicate DATA_RADIX directive found.", directive)
                            dataRadixEnum = parsePsiRadixValue(directive.value)
                        }
                    }
                } catch (e: MifSemanticException) {
                    throw e // Re-throw specific semantic errors
                } catch (e: Exception) {
                    // Wrap unexpected errors during directive processing
                    throw MifSemanticException("Error processing directive ${directive::class.simpleName}: ${e.message}", directive, e)
                }
            }

            // 2. Validate Directives Presence
            val finalWidthBI = widthBI ?: throw MifSemanticException("Missing WIDTH directive.")
            val finalDepthBI = depthBI ?: throw MifSemanticException("Missing DEPTH directive.")
            val finalAddressRadixEnum = addressRadixEnum ?: throw MifSemanticException("Missing ADDRESS_RADIX directive.")
            val finalDataRadixEnum = dataRadixEnum ?: throw MifSemanticException("Missing DATA_RADIX directive.")

            // 3. Determine Target Types (Address and Word)
            val finalAddrType: UnsignedFixedSizeIntNumberT<*> = try { // Explicitly unsigned for address
                determineAddressType(finalDepthBI)
            } catch (e: MifSemanticException) {
                val depthDirective = directives.firstOrNull { it is MifDirective.Depth }
                throw MifSemanticException(e.message ?: "Failed to determine address type from depth", depthDirective, e)
            }

            val finalWordType: FixedSizeIntNumberT<*> = try {
                // Convert width to Int, handling potential overflow if BigInt is truly huge
                val widthInt = try {
                    finalWidthBI.toInt()
                } catch (e: Exception) {
                    throw MifSemanticException("WIDTH value ($finalWidthBI) is too large.", directives.firstOrNull { it is MifDirective.Width }, e)
                }
                determineWordType(widthInt, finalDataRadixEnum)
            } catch (e: MifSemanticException) {
                val widthDirective = directives.firstOrNull { it is MifDirective.Width }
                throw MifSemanticException(e.message ?: "Failed to determine word type from width/radix", widthDirective, e)
            }

            // 4. Process Content Block
            val tempMemory = mutableMapOf<UnsignedFixedSizeIntNumber<*>, FixedSizeIntNumber<*>>() // Store BigInt -> BigInt temporarily
            if (content != null) {
                content.entries.forEach loop@{ entry ->
                    val addressSpec = entry.addressSpec
                    if (entry.dataValues.isEmpty()) throw MifSemanticException("Content entry missing data value(s).", entry)

                    try {
                        // Parse data values first
                        val dataValuesBI = entry.dataValues.map { parsePsiNumericValue(it, finalDataRadixEnum) }

                        if (dataValuesBI.all { it == BigInt.ZERO }) return@loop

                        // Resolve addresses based on spec type
                        val addressesBI: List<BigInt> = when (addressSpec) {
                            is MifAddressSpec.Single -> listOf(resolveSingleAddress(addressSpec, finalAddressRadixEnum))
                            is MifAddressSpec.Range -> resolveRangeAddresses(addressSpec, finalAddressRadixEnum)
                            is MifAddressSpec.RangeList -> resolveListAddresses(addressSpec, finalAddressRadixEnum)
                            // else case should not happen if parser produced valid structure
                        }

                        // Assign values to tempMemory, handling sequences and bounds checking
                        addressesBI.forEachIndexed { sequenceIndex, addr ->
                            // Check bounds using BigInt depth
                            if (addr < BigInt.ZERO || addr >= finalDepthBI) {
                                throw MifSemanticException(
                                    "Address '${addr.toString(finalAddressRadixEnum.base)}' (from spec '${addressSpec}') is out of range [0..${finalDepthBI - 1}].",
                                    addressSpec // Use specific spec element for error context
                                )
                            }

                            // Determine data value (repeating if necessary)
                            val dataIndex = sequenceIndex % dataValuesBI.size
                            val valueToAssign = dataValuesBI[dataIndex]

                            tempMemory[finalAddrType.to(addr)] = finalWordType.to(valueToAssign)
                        }
                    } catch (e: MifSemanticException) {
                        throw e // Re-throw specific semantic errors
                    } catch (e: Exception) {
                        throw MifSemanticException("Error processing content entry: ${e.message}", entry, e)
                    }
                }
            } // End if content != null

            // 5. Consolidate Memory Sections using typed addresses and words
            // Casts are needed here because the exact ADDR/WORD types are only known at runtime within this function.
            val finalMemory: Map<UnsignedFixedSizeIntNumber<*>, List<FixedSizeIntNumber<*>>> = consolidateMemorySectionsTyped(
                tempMemory, // Cast Map<IntNumber<*>, IntNumber<*>>
                finalAddrType // Pass address type info
            )


            // 6. Construct and Return MifData
            // Necessary casts due to runtime type determination for generics
            return MifData(
                addrWidth = finalAddrType, // Cast ADDR type
                width = finalWordType,     // Cast WORD type
                depth = finalDepthBI,
                addressRadix = finalAddressRadixEnum,
                dataRadix = finalDataRadixEnum,
                memory = finalMemory // Map is already Map<ADDR, List<WORD>> from consolidation (with casts)
            )
        }

        // --- Helper Functions ---

        /** Determines the smallest standard Unsigned type needed to hold addresses up to depth-1 */
        private fun determineAddressType(depth: BigInt): UnsignedFixedSizeIntNumberT<*> {
            if (depth <= BigInt.ZERO) throw MifSemanticException("DEPTH must be positive to determine address type.")
            if (depth == BigInt.ONE) return UInt8 // Special case: depth 1 needs at least 1 bit (UInt8 smallest)

            // Calculate bits needed for highest address (depth - 1)
            val highestAddress = depth - BigInt.ONE
            val bitsNeeded = highestAddress.value.bitLength() // Number of bits required

            return when {
                bitsNeeded <= 8 -> UInt8
                bitsNeeded <= 16 -> UInt16
                bitsNeeded <= 32 -> UInt32
                bitsNeeded <= 64 -> UInt64
                bitsNeeded <= 128 -> UInt128
                else -> throw MifSemanticException("Calculated address width ($bitsNeeded bits) exceeds supported types (max 128).")
            }
        }

        /** Determines the IntNumber type based on width and data radix (signed/unsigned) */
        private fun determineWordType(width: Int, dataRadix: MifRadix): FixedSizeIntNumberT<*> {
            if (width <= 0) throw MifSemanticException("WIDTH must be positive.")
            return if (dataRadix == MifRadix.UNS) { // Check if explicitly unsigned
                when (width) {
                    8 -> UInt8
                    16 -> UInt16
                    32 -> UInt32
                    64 -> UInt64
                    128 -> UInt128
                    else -> throw MifSemanticException("Unsupported unsigned data width: $width")
                }
            } else { // Default to signed for HEX, DEC, BIN, OCT
                when (width) {
                    8 -> Int8
                    16 -> Int16
                    32 -> Int32
                    64 -> Int64
                    128 -> Int128
                    else -> throw MifSemanticException("Unsupported signed data width: $width")
                }
            }
        }

        /** Consolidates memory sections, now working with typed addresses and words */
        private fun consolidateMemorySectionsTyped(
            individualEntries: Map<UnsignedFixedSizeIntNumber<*>, FixedSizeIntNumber<*>>,
            addrType: UnsignedFixedSizeIntNumberT<*>, // Pass type info for calculations
        ): Map<UnsignedFixedSizeIntNumber<*>, List<FixedSizeIntNumber<*>>> {
            if (individualEntries.isEmpty()) {
                return emptyMap()
            }

            // Sort keys (addresses). Requires ADDR to be Comparable. IntNumber should be.
            val sortedAddresses: List<UnsignedFixedSizeIntNumber<*>> = individualEntries.keys.sortedBy { it.toBigInt() } // Assuming IntNumber implements Comparable
            val sectionMap = mutableMapOf<UnsignedFixedSizeIntNumber<*>, MutableList<FixedSizeIntNumber<*>>>()

            var currentSectionStartAddr: UnsignedFixedSizeIntNumber<*>? = null
            var currentSectionValues: MutableList<FixedSizeIntNumber<*>>? = null

            for (currentAddr in sortedAddresses) {
                val currentValue = individualEntries[currentAddr] ?: continue // Should not happen

                if (currentSectionStartAddr == null || currentSectionValues == null) {
                    // Start the very first section
                    currentSectionStartAddr = currentAddr
                    currentSectionValues = mutableListOf(currentValue)
                    sectionMap[currentSectionStartAddr] = currentSectionValues
                } else {
                    // Calculate expected next address using BigInt logic for safety/generality
                    val startBI = currentSectionStartAddr.toBigInt()
                    val sizeBI = currentSectionValues.size.toBigInt()
                    val expectedNextBI = startBI + sizeBI // Equivalent to startBI + BigInt(currentSectionValues.size)

                    // Convert expected BigInt back to ADDR type for comparison
                    val expectedNextAddr: UnsignedFixedSizeIntNumber<*> = try {
                        addrType.to(expectedNextBI)
                    } catch (e: Exception) {
                        // This might happen if depth calculation was wrong or consolidation logic has edge cases
                        throw MifSemanticException("Internal error during consolidation: Calculated next address $expectedNextBI exceeds limits for type ${addrType}.", null, e)
                    }

                    // Compare typed addresses
                    if (currentAddr == expectedNextAddr) {
                        // Yes, continue current section
                        currentSectionValues.add(currentValue)
                    } else {
                        // No, gap detected. Start a new section.
                        currentSectionStartAddr = currentAddr
                        currentSectionValues = mutableListOf(currentValue)
                        sectionMap[currentSectionStartAddr] = currentSectionValues
                    }
                }
            }
            // Convert inner mutable lists to immutable lists for the final result
            return sectionMap.mapValues { it.value.toList() }
        }


        // --- PSI Node Parsing Helpers (mostly unchanged, ensure correct value access) ---

        private fun parsePsiNumericValue(node: MifNumericValue, radix: MifRadix): BigInt {
            // Access the token's string value
            val text = node.valueToken.value.trim()
            try {
                return BigInt.parse(text, radix.base)
            } catch (e: NumberFormatException) {
                throw MifSemanticException("Invalid number format for ${radix.name} value '$text'.", node, e)
            }
        }

        private fun parsePsiRadixValue(node: MifRadixValue): MifRadix {
            val text = node.radixToken?.value?.uppercase() // Access token's string value
                ?: throw MifSemanticException("Missing radix keyword.", node)
            return MifRadix.fromString(text)
                ?: throw MifSemanticException("Invalid radix value: '$text'.", node)
        }

        private fun resolveSingleAddress(node: MifAddressSpec.Single, radix: MifRadix): BigInt {
            // Access the token's string value
            val text = node.addressValue.value.trim()
            try {
                return BigInt.parse(text, radix.base)
            } catch (e: NumberFormatException) {
                throw MifSemanticException("Invalid number format for ${radix.name} address '$text'.", node, e)
            }
        }

        private fun resolveRangeAddresses(node: MifAddressSpec.Range, radix: MifRadix): List<BigInt> {
            // MifAddressSpec.Range structure might contain MifAddressSpec.Single nodes
            // Adjust access based on actual PSI structure from previous steps
            val startNode = node.startAddressValue // Assuming this returns MifAddressSpec.Single
                ?: throw MifSemanticException("Missing start address in range specification.", node)
            val endNode = node.endAddressValue   // Assuming this returns MifAddressSpec.Single
                ?: throw MifSemanticException("Missing end address in range specification.", node)

            val startAddr = resolveSingleAddress(startNode, radix)
            val endAddr = resolveSingleAddress(endNode, radix)

            if (startAddr > endAddr) {
                throw MifSemanticException("Start address ($startAddr) cannot be greater than end address ($endAddr) in range.", node)
            }

            val addresses = mutableListOf<BigInt>()
            var current = startAddr
            while (current <= endAddr) {
                addresses.add(current)
                current += BigInt.ONE // Increment BigInt
            }
            return addresses
        }

        private fun resolveListAddresses(node: MifAddressSpec.RangeList, radix: MifRadix): List<BigInt> {
            val valueNodes = node.addressValues // Assuming this returns List<MifAddressSpec.Single>
            if (valueNodes.isEmpty()) {
                throw MifSemanticException("Empty address list specification.", node)
            }
            return valueNodes.map { resolveSingleAddress(it, radix) }
        }
    }
}