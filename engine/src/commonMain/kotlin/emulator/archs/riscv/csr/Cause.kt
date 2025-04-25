package emulator.archs.riscv.csr

// --- Exception Cause Codes ---
object Cause {
    const val INSTR_ADDR_MISALIGNED = 0L
    const val INSTR_ACCESS_FAULT = 1L
    const val ILLEGAL_INSTRUCTION = 2L
    const val BREAKPOINT = 3L
    const val LOAD_ADDR_MISALIGNED = 4L
    const val LOAD_ACCESS_FAULT = 5L
    const val STORE_AMO_ADDR_MISALIGNED = 6L
    const val STORE_AMO_ACCESS_FAULT = 7L
    const val ECALL_USER = 8L
    const val ECALL_SUPERVISOR = 9L
    const val ECALL_MACHINE = 11L
    const val INSTR_PAGE_FAULT = 12L
    const val LOAD_PAGE_FAULT = 13L
    const val STORE_AMO_PAGE_FAULT = 15L
}