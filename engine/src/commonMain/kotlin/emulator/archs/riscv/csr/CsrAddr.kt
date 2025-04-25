package emulator.archs.riscv.csr

// CSR Addresses (Ensure these match your RvRegT or define them)
object CsrAddr {
    const val MSTATUS = 0x300
    const val SSTATUS = 0x100
    const val MTVEC = 0x305
    const val STVEC = 0x105
    const val MEPC = 0x341
    const val SEPC = 0x141
    const val MCAUSE = 0x342
    const val SCAUSE = 0x142
    const val MTVAL = 0x343
    const val STVAL = 0x143
    // Add UEPC, UCAUSE, UTVAL, UTVEC, USTATUS if User traps are handled
    const val UEPC = 0x041
    const val UCAUSE = 0x042
    const val UTVAL = 0x043
    const val UTVEC = 0x045
    const val USTATUS = 0x000
}