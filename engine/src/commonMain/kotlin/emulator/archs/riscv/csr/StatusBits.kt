package emulator.archs.riscv.csr

// MSTATUS/SSTATUS Bits/Fields for RV64 (positions)
object StatusBits {
    const val UIE_BIT = 0
    const val SIE_BIT = 1
    const val MIE_BIT = 3
    const val UPIE_BIT = 4
    const val SPIE_BIT = 5
    const val MPIE_BIT = 7
    const val SPP_BIT = 8  // Prev Privilege S-Mode (0=U, 1=S)
    const val MPP_SHIFT = 11 // Prev Privilege M-Mode (00=U, 01=S, 11=M)
    const val MPP_MASK = 3
    const val FS_SHIFT = 13 // Floating Point Status
    const val FS_MASK = 3
    const val XS_SHIFT = 15 // User Extension Status
    const val XS_MASK = 3
    const val MPRV_BIT = 17 // Modify Privilege (M-Mode)
    const val SUM_BIT = 18 // Permit Supervisor User Memory access
    const val MXR_BIT = 19 // Make eXecutable Readable
    const val TVM_BIT = 20 // Trap Virtual Memory (S-Mode)
    const val TW_BIT = 21  // Timeout Wait (S-Mode)
    const val TSR_BIT = 22 // Trap SRET (S-Mode)
    const val SXL_SHIFT = 34 // Supervisor XLEN
    const val SXL_MASK = 3
    const val UXL_SHIFT = 32 // User XLEN
    const val UXL_MASK = 3
    const val SD_BIT = 63 // Status Dirty (FS/XS dirty indicator)

    // Helper masks for convenience
    const val UIE = 1L shl UIE_BIT
    const val SIE = 1L shl SIE_BIT
    const val MIE = 1L shl MIE_BIT
    const val UPIE = 1L shl UPIE_BIT
    const val SPIE = 1L shl SPIE_BIT
    const val MPIE = 1L shl MPIE_BIT
    const val SPP = 1L shl SPP_BIT
    const val MPRV = 1L shl MPRV_BIT
    const val SUM = 1L shl SUM_BIT
    const val MXR = 1L shl MXR_BIT
    const val TVM = 1L shl TVM_BIT
    const val TW = 1L shl TW_BIT
    const val TSR = 1L shl TSR_BIT
    const val SD = 1L shl SD_BIT
}