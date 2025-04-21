# RISC-V 64 Bit example

#################################################################################
# CONSTANTS
#################################################################################

.equ LED_BASE_ADDR, 0x8000000000000000
.equ UART_BASE_ADDR, 0x8000000000000001
.equ USB_BASE_ADDR, 0x8000000000000004

.equ ACLINT_MTIME, 0x8000000000001824

.equ RAM_START, 0x800

.equ SOH, 0x1
.equ EOT, 0x4
.equ ACK, 0x6
.equ NAK, 0x15

.equ ELF_MAGIC, 0x464c457f
.equ ELF_DATA_CLASS, 0x0102 #64-Bit little-endian
.equ ELF_TYPE, 0x02 #Executable
.equ ELF_MACHINE, 0xF3 #RISC-V

.equ ELF_SHT_PROGBITS, 0x1
.equ ELF_SHT_RELA, 0x4
.equ ELF_SHT_NOBITS, 0x8

#################################################################################
# PROGRAM
#################################################################################

.globl _start

##############################################
##                  SETUP                   ##
##############################################

_start:     li     sp, 0x2000

            li     t0, 0xFF
            li     t1, LED_BASE_ADDR
            sb     t0, 0(t1)

            #print info string
            la     a0, infoStr
            jal     s11, uart.outStr 

            li     t1, LED_BASE_ADDR
            li     t0, 0x2
            sb     t0, 0(t1)

            #copy symbol table addresses to RAM
            addi    sp, sp,(-8*6)
            la      a0, elf.checkElfHeader
            sd      a0, (5*8)(sp)
            la      a0, elf.relocateProgram
            sd      a0, (4*8)(sp)
            la      a0, uart.in
            sd      a0, (3*8)(sp)
            la      a0, uart.out
            sd      a0, (2*8)(sp)
            la      a0, uart.outStr
            sd      a0, (1*8)(sp)
            la      a0, xmodem.receiveData
            sd      a0, 0(sp)

            #copy symbol table addresses to RAM
            la      t2, symbolTableStrings #start
            la      t3, infoStr #end
s.stNext:   bgeu    t2,t3,s.stDone
            addi    sp, sp,-8
            addi    t3, t3,-8
            ld      t1, 0(t3)
            sd      t1, 0(sp)
            j       s.stNext

            #print waiting for uart/usb string
s.stDone:   la     a0, directionsStr
            jal     s11, uart.outStr 

            li     t1, LED_BASE_ADDR
            li     t0, 0x80
            sb     t0, 0(t1)

##############################################
##            WAITING FOR CODE              ##
##############################################

#Send NAK to signal transmitter that we are ready
_wait:      li      a0, NAK
            jal     uart.out
            li      a0, 0
            jal     uart.out
#Check if we receive an SOH in our RX Port
            li      t1, ACLINT_MTIME
            ld      t2, 0(t1)
            li      t3, 12500000
            add     t3, t2,t3
            li      t4, SOH
xwft.checkRX:
            jal     uart.in
            beq     a0,t4,xmodem.receiveData
            ld      t2, 0(t1)
            bgeu    t3,t2,xwft.checkRX

#Waited 4 Seconds and did not detect a SOH            
            li     t1, LED_BASE_ADDR
            lbu     t0, 0(t1)
            srli    t0, t0,1
            bnez    t0, xwft.ledOk
            li     t0, 0x80
 xwft.ledOk:sb     t0, 0(t1)

            j       _wait


##############################################
##                  EXIT                    ##
##############################################

_end:       li     t1, LED_BASE_ADDR
            li     t0, 0x8
            sb     t0, 0(t1)

            la     a0, receivedStr
            jal     s11, uart.outStr 

            li      s0, 0x800   #ELF-Start
            #      s3            Address of Program entry after reallocation
            #      s4            Address of ELF-End in RAM

            mv      a0, s0
            jal     elf.checkElfHeader
            beqz    a0, headerOk
            j       _error

headerOk:   mv      s3, a3
            add     a0, s0,a4
            mv      a4, s0
            jal     elf.relocateProgram
            
            li     t1, LED_BASE_ADDR
            li     t0, 0x16
            sb     t0, 0(t1)

            #clear ELF Residue in RAM
nxtClr:     bgeu    t3,s4,clrEnd
            sb      zero, 0(t3)
            addi    t3, t3,1
            j       nxtClr
clrEnd:

            mv      ra, s3

            mv      sp, zero
            #gp not used
            #tp not used
            mv      t0, zero
            mv      t1, zero
            mv      t2, zero
            mv      s0, zero
            mv      s1, zero
            mv      a0, zero
            mv      a1, zero
            mv      a2, zero
            mv      a3, zero
            mv      a4, zero
            mv      a5, zero
            mv      a6, zero
            mv      a7, zero
            mv      s2, zero
            mv      s3, zero
            mv      s4, zero
            mv      s5, zero
            mv      s6, zero
            mv      s7, zero
            mv      s8, zero
            mv      s9, zero
            mv      s10, zero
            mv      s11, zero
            mv      t3, zero
            mv      t4, zero
            mv      t5, zero
            mv      t6, zero
#            fence.i
            jr     ra
 
_error:     mv      s0, a0
            la      a0, errorStr
            jal     s11, uart.outStr
            mv      a0, s0
            jal     s11, uart.outStr
_doom:      j       _doom


##############################################
##                  ELF                    ##
##############################################

elf.checkElfHeader:
            li      t0, ELF_MAGIC
            lwu     t1, 0(a0)
            beq     t0,t1,ecfm.magicOk
            la      a0, noMagicStr
            ret
ecfm.magicOk:
            li      t0, ELF_DATA_CLASS
            lhu     t1, 4(a0)
            beq     t0,t1,ecfm.is64BitsLE
            la      a0, not64BitLEStr
            ret
ecfm.is64BitsLE:
            li      t0, ELF_TYPE
            lbu     t1, 16(a0)
            beq     t0,t1,ecfm.isExecutable
            la      a0, notExecutable
            ret
ecfm.isExecutable:
            li      t0, ELF_MACHINE
            lbu     t1, 18(a0)
            beq     t0,t1,ecfm.isRiscV
            la      a0, notRISCV
            ret
ecfm.isRiscV:
            lhu     a1, 58(a0) #Size of one Section Header Table Entry
            lhu     a2, 60(a0) #Number of entries in Section Header Table
            ld      a3, 24(a0) #Pointer to Program entry after reallocation
            ld      a4, 40(a0) #Offset to Section Header Table
            li      a0, 0
            ret


elf.relocateProgram:
            li      t6, ELF_SHT_NOBITS
            li      t5, ELF_SHT_PROGBITS
            li      t4, ELF_SHT_RELA
            li      a3, 0
            li      a6, 0

eis.nextS:  lwu     t0, 4(a0)
            bne     t0,t5,eis.notProg
            add     a7, a0,a1
            lwu     a7, 4(a7)
            bne     a7,t4,eis.noRel
            jal     t0, elf.resolveRelas
eis.noRel:  jal     t0, elf.moveProgramSection
            j       eis.secDone
eis.notProg:bne     t0,t6,eis.secDone
            jal     t0, elf.initBssSection

eis.secDone:add     a0, a0,a1
            addi    a3, a3,1
            bltu    a3,a2,eis.nextS
            ret

elf.moveProgramSection:
            ld      t3, 16(a0)  #address where program should be stored to
            ld      t4, 24(a0)
            add     t4, t4,a4   #address of program in File
            ld      t2, 32(a0)  
            add     t2, t2,t4   #address of program end in File
emps.next:  bgeu    t4,t2,emps.endC
            ld      t1, 0(t4)
            sd      t1, 0(t3)
            addi    t4, t4,8
            addi    t3, t3,8
            j       emps.next
emps.endC:  sub     t1, t4,t2
            beqz    t1, emps.end
            sb      zero, 0(t3)
            addi    t3, t3,-1
            addi    t4, t4,-1
            j       emps.endC
emps.end:   jr      t0

elf.initBssSection:
            ld      t3, 16(a0)  #address where program should be stored to
            ld      t2, 32(a0)  #sections size in bytes
eibs.next:  beqz    t2, eibs.end
            sb      zero, 0(t3)
            addi    t2, t2,-1
            j       eibs.next
eibs.end:   jr      t0


elf.resolveRelas:



##############################################
##             UART & XMODEM                ##
##############################################

#.globl xmodem.receiveData
#.globl uart.out
#.globl uart.outStr
#.globl uart.in

#XMODEM:    TX          RX      Description
#0                      NAK     Tells TX that RX is ready
#1          SOH                 Tells RX that transmission is starting
#2          PaketNo             Tell RX what the next Packet Number is
#2          -PaketNo            Send the negative of this packet number as error control
#3          128 Bytes           Send Payload
#4          Checksum            Checksum (here: all 128 byte values added together) for RX to compare
#5                      ACK/NAK depending on if the checksums matched or not
#6          EOT                 If the TX is done, they will send an EOT, otherwise continue with step #1
#7                      ACK     Acknowlege End of Transfer
xmodem.receiveData:
            li      s0, 1   #next expected packet number (starts at 1)
#           li      s2, 0   #received bytes counter
#           li      s3, 0   #checksum
            li      s4, 0x800   #Pointer to Instruction Memory
            li      s5, 128
            li      s6, ACK
            li      s7, NAK
            li      s8, SOH
            li      s9, EOT

xrd.nextPacket:
            #check if EOT or SOH
#            jal     uart.in    #already uart.in'd before subroutine call
            beq     a0,s9,xrd.endOfTransfer
            beq     a0,s8,xrd.startOfHeading
            li      a1, 1 ####
            j       xrd.transErrorRetry
xrd.startOfHeading:
            #read and check bot packet numbers
            jal     uart.in
            li      a1, 2 ####
            bne     a0,s0,xrd.transErrorRetry
            jal     uart.in
            not     a0, a0
            andi    a0, a0,0xFF
            li      a1, 4 ####
            bne     a0,s0,xrd.transErrorRetry

            #start data transmission
            li      s2, 0
            li      s3, 0
xrd.nextDataByte:
            jal     uart.in
            sb      a0, 0(s4)
            addi    s2, s2,1
            add     s3, s3,a0
            addi    s4, s4,1
            bne     s2,s5,xrd.nextDataByte

            #compare checksums
            jal     uart.in
            andi    s3, s3,0xFF
            li      a1, 8 ####
            bne     a0,s3,xrd.transErrorRetry
            #checksums ok
            #increment packetNo and wait for next packet
            mv      a0, s6
            jal     uart.out
            li     t1, LED_BASE_ADDR
            li     t0, 0xF0
            sb     t0, 0(t1)
            addi    s0, s0,1
            andi    s0, s0,0xFF
            jal     uart.in
            j       xrd.nextPacket

xrd.transErrorRetry:
            li     t1, LED_BASE_ADDR
            sb     a1, 0(t1)
            mv      a0, s7
            jal     uart.out
            jal     uart.in
            j       xrd.nextPacket

xrd.endOfTransfer:
            mv      a0, s6
            jal     uart.out
            j       _end



#a0: pointer to zero terminated ascii data
uart.outStr:mv      s0, a0
uos.next:   lbu     a0, 0(s0)
            beqz    a0, uos.end
            jal     uart.out
            addi    s0, s0,1
            j       uos.next
uos.end:    jr  s11

# send character in a0
uart.out:   li  t1,UART_BASE_ADDR
uout.wait:  lb  t0, 2(t1)    # check if ty uart is busy
      bnez  t0, uout.wait
      sb  a0, 0(t1)    # write data in a0 to uart
      ret

# receive character in a0
uart.in:    li  t1,UART_BASE_ADDR
uin.wait:   lb  t0, 1(t1)    # check if transmission is completed
      beqz  t0, uin.wait
      lbu  a0, 0(t1)    # write uart rx data to a0
      sb   zero, 1(t1)    # remove rx data
      ret

.data

symbolTableStrings:
.asciz "elf.checkElfHeader"
.asciz "elf.relocateProgram"
.asciz "uart.in"
.asciz "uart.out"
.asciz "uart.outStr"
.asciz "xmodem.receiveData"
#.align 3

infoStr:  .asciz "\n\rarch: rv64i   abi: lp64   memory access: strict-align v0.13\n\r"
directionsStr: .asciz "Please insert a Mass Storage Device into the USB-Port with a .elf file\n\rin the root directory or start transmitting the ELF-Binaries via UART.\n\r"
receivedStr: .asciz "\n\rprogram reveived! starting...\n\r"
errorStr: .asciz "\r\nError: "
noMagicStr: .asciz "File does not start with ELF Magic!"
not64BitLEStr: .asciz "ELF File is not in 64-Bit little-endian format!"
notExecutable: .asciz "This ELF File is not \"EXECUTABLE\"!"
notRISCV: .asciz "This Code was not compiled for RISC-V!"


.data
#.zero 1
