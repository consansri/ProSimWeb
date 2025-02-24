package cengine.lang.asm.ast.impl

import cengine.editor.annotation.Annotation
import cengine.lang.asm.AsmPsiParser
import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.asm.ast.Component.*
import cengine.lang.asm.ast.DirTypeInterface
import cengine.lang.asm.ast.Rule
import cengine.lang.asm.ast.TargetSpec
import cengine.lang.asm.ast.lexer.AsmLexer
import cengine.lang.asm.ast.lexer.AsmTokenType
import cengine.lang.obj.elf.Shdr
import cengine.util.integer.BigInt
import cengine.vfs.FPath
import cengine.vfs.VFileSystem

enum class ASDirType(
    val contentStartsDirectly: Boolean = false,
    override val isSection: Boolean = false,
    override val rule: Rule? = null,
) : DirTypeInterface {
    ABORT(rule = Rule.dirNameRule("abort")),
    ALIGN(rule = Rule {
        Seq(
            Specific(".align", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable(maxLength = 2) {
                        Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    ALTMACRO(rule = Rule.dirNameRule("altmacro")),
    ASCII(rule = Rule {
        Seq(
            Specific(".ascii", ignoreCase = true),
            Optional {
                Seq(
                    Repeatable { SpecNode(ASNodeType.STRING_EXPR) },
                    Repeatable {
                        Seq(Specific(","), Repeatable { SpecNode(ASNodeType.STRING_EXPR) })
                    }
                )
            }
        )
    }),
    ASCIZ(rule = Rule {
        Seq(
            Specific(".asciz", ignoreCase = true),
            Optional {
                Seq(
                    Repeatable { SpecNode(ASNodeType.STRING_EXPR) },
                    Repeatable {
                        Seq(Specific(","), Repeatable { SpecNode(ASNodeType.STRING_EXPR) })
                    }
                )
            }
        )
    }),
    ATTACH_TO_GROUP_NAME(rule = Rule.dirNameRule("attach_to_group_name")),
    BALIGN(rule = Rule {
        Seq(
            Specific(".balign", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    BALIGNL(rule = Rule {
        Seq(
            Specific(".balignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )

    }),
    BALIGNW(rule = Rule {
        Seq(
            Specific(".balignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )

    }),
    BSS(isSection = true, rule = Rule {
        Seq(
            Specific(".bss", ignoreCase = true),
            Optional { InSpecific(AsmTokenType.SYMBOL) }
        )
    }),
    BYTE(rule = Rule {
        Seq(
            Specific(".byte", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )

    }),
    COMM(rule = Rule {
        Seq(
            Specific(".comm", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    DATA(isSection = true, rule = Rule {
        Seq(
            Specific(".data", ignoreCase = true),
            Optional { InSpecific(AsmTokenType.SYMBOL) }
        )
    }),
    DEF(rule = Rule {
        Seq(
            Specific(".def", ignoreCase = true),
            Repeatable { Except(Dir("ENDEF")) },
            Dir("ENDEF")
        )
    }),
    DESC(rule = Rule {
        Seq(
            Specific(".desc", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    DIM(rule = Rule.dirNameRule("dim")),
    DOUBLE(rule = Rule.dirNameRule("double")),
    EJECT(rule = Rule.dirNameRule("eject")),
    ELSE(rule = Rule.dirNameRule("else")),
    ELSEIF(rule = Rule.dirNameRule("elseif")),
    END(rule = Rule.dirNameRule("end")),
    ENDM(rule = Rule.dirNameRule("endm")),
    ENDR(rule = Rule.dirNameRule("endr")),
    ENDEF(rule = Rule.dirNameRule("endef")),
    ENDFUNC(rule = Rule.dirNameRule("endfunc")),
    ENDIF(rule = Rule.dirNameRule("endif")),
    EQU(rule = Rule {
        Seq(
            Specific(".equ", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    EQUIV(rule = Rule {
        Seq(
            Specific(".equiv", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    EQV(rule = Rule {
        Seq(
            Specific(".eqv", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    ERR(rule = Rule.dirNameRule("err")),
    ERROR(rule = Rule {
        Seq(
            Specific(".error", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    EXITM(rule = Rule.dirNameRule("exitm")),
    EXTERN(rule = Rule.dirNameRule("extern")),
    FAIL(rule = Rule {
        Seq(
            Specific(".fail", ignoreCase = true),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    FILE(rule = Rule {
        Seq(
            Specific(".file", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    FILL(rule = Rule {
        Seq(
            Specific(".fill", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    FLOAT(rule = Rule.dirNameRule("float")),
    FUNC(rule = Rule {
        Seq(
            Specific(".func", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Optional { Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL)) }
        )
    }),
    GLOBAL(rule = Rule {
        Seq(
            Specific(".global", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    GLOBL(rule = Rule {
        Seq(
            Specific(".globl", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    GNU_ATTRIBUTE(rule = Rule.dirNameRule("gnu_attribute")),
    HIDDEN(rule = Rule {
        Seq(
            Specific(".hidden", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Repeatable { Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL)) }
        )
    }),
    HWORD(rule = Rule {
        Seq(
            Specific(".hword", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                    }
                )
            }
        )
    }),
    IDENT(rule = Rule.dirNameRule("ident")),
    IF(rule = Rule {
        Seq(
            Specific(".if", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFDEF(rule = Rule {
        Seq(
            Specific(".ifdef", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(
                    Dir("elseif"),
                    InSpecific(AsmTokenType.SYMBOL),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFB(rule = Rule {
        Seq(
            Specific(".ifb", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFC(rule = Rule {
        Seq(
            Specific(".ifc", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Specific(","),
            SpecNode(ASNodeType.STRING_EXPR),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFEQ(rule = Rule {
        Seq(
            Specific(".ifeq", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            },
            Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            },
            Dir("endif")
        )
    }),
    IFEQS(rule = Rule {
        Seq(
            Specific(".ifeqs", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Specific(","),
            SpecNode(ASNodeType.STRING_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Specific(","),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFGE(rule = Rule {
        Seq(
            Specific(".ifge", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFGT(rule = Rule {
        Seq(
            Specific(".ifgt", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFLE(rule = Rule {
        Seq(
            Specific(".ifle", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFLT(rule = Rule {
        Seq(
            Specific(".iflt", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNB(rule = Rule {
        Seq(
            Specific(".ifnb", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNC(rule = Rule {
        Seq(
            Specific(".ifnc", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR), Specific(","), SpecNode(ASNodeType.STRING_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Specific(","),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNDEF(rule = Rule {
        Seq(
            Specific(".ifndef", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    InSpecific(AsmTokenType.SYMBOL),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNOTDEF(rule = Rule {
        Seq(
            Specific(".ifnotdef", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    InSpecific(AsmTokenType.SYMBOL),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNE(rule = Rule {
        Seq(
            Specific(".ifne", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Specific(","),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    IFNES(rule = Rule {
        Seq(
            Specific(".ifnes", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR), Specific(","), SpecNode(ASNodeType.STRING_EXPR), Repeatable {
                Except(XOR(Dir("endif"), Dir("else"), Dir("elseif")))
            }, Repeatable {
                Seq(
                    Dir("elseif"),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Specific(","),
                    SpecNode(ASNodeType.STRING_EXPR),
                    Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Optional {
                Seq(Dir("else"), Repeatable { Except(XOR(Dir("endif"), Dir("else"), Dir("elseif"))) })
            }, Dir("endif")
        )
    }),
    INCBIN(rule = Rule {
        Seq(
            Specific(".incbin", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR), Optional {
                Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR), Optional {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    INCLUDE(rule = Rule {
        Seq(
            Specific(".include", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    INT(rule = Rule {
        Seq(
            Specific(".int", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    INTERNAL(rule = Rule {
        Seq(
            Specific(".internal", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL))
            }
        )
    }),
    IRP(rule = Rule {
        Seq(
            Specific(".irp", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Repeatable {
                Seq(
                    Specific(","),
                    Except(XOR(Dir("ENDR"), InSpecific(AsmTokenType.LINEBREAK))),
                )
            },
            Repeatable {
                Except(Dir("ENDR"))
            }, Dir("ENDR")
        )
    }),
    IRPC(rule = Rule {
        Seq(
            Specific(".irpc", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            Optional { Except(XOR(Dir("ENDR"), InSpecific(AsmTokenType.LINEBREAK))) },
            Repeatable {
                Except(Dir("ENDR"))
            }, Dir("ENDR")
        )
    }),
    LCOMM(rule = Rule {
        Seq(
            Specific(".lcomm", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Specific(","), SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    LFLAGS(rule = Rule.dirNameRule("lflags")),
    LINE(rule = Rule {
        Seq(
            Specific(".line", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    LINKONCE(rule = Rule {
        Seq(
            Specific(".linkonce", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    LIST(rule = Rule.dirNameRule("list")),
    LN(rule = Rule {
        Seq(
            Specific(".ln", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    LOC(rule = Rule.dirNameRule("loc")),
    LOC_MARK_LABELS(rule = Rule.dirNameRule("loc_mark_labels")),
    LOCAL(rule = Rule {
        Seq(
            Specific(".local", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL))
            }
        )
    }),
    LONG(rule = Rule {
        Seq(
            Specific(".long", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    MACRO(rule = Rule {
        Seq(
            Specific(".macro", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Optional {
                Seq(
                    SpecNode(ASNodeType.ARG),
                    Repeatable {
                        Seq(
                            Optional {
                                Specific(",")
                            },
                            SpecNode(ASNodeType.ARG)
                        )
                    }
                )
            }
        )
    }),
    MRI(rule = Rule {
        Seq(
            Specific(".mri", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    NOALTMACRO(rule = Rule.dirNameRule("noaltmacro")),
    NOLIST(rule = Rule.dirNameRule("nolist")),
    NOP(rule = Rule {
        Seq(
            Specific(".nop", ignoreCase = true),
            Optional {
                SpecNode(ASNodeType.INT_EXPR)
            }
        )
    }),
    NOPS(rule = Rule {
        Seq(
            Specific(".nops", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
            }
        )
    }),
    OCTA(rule = Rule {
        Seq(
            Specific(".octa", ignoreCase = true),
            Optional {
                Seq(
                    SpecNode(ASNodeType.INT_EXPR),
                    Repeatable {
                        Seq(
                            Specific(","),
                            SpecNode(ASNodeType.INT_EXPR)
                        )
                    }
                )
            }
        )
    }),
    OFFSET(rule = Rule {
        Seq(
            Specific(".offset", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    ORG(rule = Rule.dirNameRule("org")),
    P2ALIGN(rule = Rule {
        Seq(
            Specific(".p2align", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    P2ALIGNW(rule = Rule {
        Seq(
            Specific(".p2alignw", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    P2ALIGNL(rule = Rule {
        Seq(
            Specific(".p2alignl", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable(maxLength = 2) {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    POPSECTION(rule = Rule.dirNameRule("popsection")),
    PREVIOUS(rule = Rule.dirNameRule("previous")),
    PRINT(rule = Rule {
        Seq(
            Specific(".print", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    PROTECTED(rule = Rule {
        Seq(
            Specific(".protected", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL), Repeatable {
                Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL))
            }
        )
    }),
    PSIZE(rule = Rule {
        Seq(
            Specific(".psize", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    PURGEM(rule = Rule {
        Seq(
            Specific(".purgem", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    PUSHSECTION(rule = Rule {
        Seq(
            Specific(".pushsection", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Optional {
                Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL))
            }
        )
    }),
    QUAD(rule = Rule {
        Seq(
            Specific(".quad", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    RODATA(isSection = true, rule = Rule {
        Seq(
            Specific(".rodata", ignoreCase = true),
            Optional {
                InSpecific(AsmTokenType.SYMBOL)
            }
        )
    }),
    RELOC(rule = Rule {
        Seq(
            Specific(".reloc", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Specific(","),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    REPT(rule = Rule {
        Seq(
            Specific(".rept", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    SBTTL(rule = Rule {
        Seq(
            Specific(".sbttl", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    SCL(rule = Rule {
        Seq(
            Specific(".scl", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    SECTION(isSection = true, rule = Rule {
        Seq(
            Specific(".section", ignoreCase = true),
            XOR(
                Dir("DATA"), Dir("text"), Dir("RODATA"), Dir("BSS"), Seq(
                    InSpecific(AsmTokenType.SYMBOL),
                    Optional {
                        Seq(
                            Specific(","),
                            SpecNode(ASNodeType.STRING_EXPR)
                        )
                    }
                ))
        )
    }),
    SET_ALT(contentStartsDirectly = true, rule = Rule {
        Seq(
            InSpecific(AsmTokenType.SYMBOL),
            Specific("="),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    SET(rule = Rule {
        Seq(
            Specific(".set", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.ANY_EXPR)
        )
    }),
    SHORT(rule = Rule {
        Seq(
            Specific(".short", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    SINGLE(rule = Rule.dirNameRule("single")),
    SIZE(rule = Rule.dirNameRule("size")),
    SKIP(rule = Rule {
        Seq(
            Specific(".skip", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
            }
        )
    }),
    SLEB128(rule = Rule {
        Seq(
            Specific(".sleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    SPACE(rule = Rule {
        Seq(
            Specific(".space", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
            }
        )
    }),
    STABD(rule = Rule.dirNameRule("stabd")),
    STABN(rule = Rule.dirNameRule("stabn")),
    STABS(rule = Rule.dirNameRule("stabs")),
    STRING(rule = Rule {
        Seq(
            Specific(".string", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.STRING_EXPR))
            }
        )
    }),
    STRING8(rule = Rule {
        Seq(
            Specific(".string8", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.STRING_EXPR))
            }
        )
    }),
    STRING16(rule = Rule {
        Seq(
            Specific(".string16", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.STRING_EXPR))
            }
        )
    }),
    STRING32(rule = Rule {
        Seq(
            Specific(".string32", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR),
            Optional {
                Seq(Specific(","), SpecNode(ASNodeType.STRING_EXPR))
            }
        )
    }),
    STRUCT(rule = Rule {
        Seq(
            Specific(".struct", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    SUBSECTION(rule = Rule {
        Seq(
            Specific(".subsection", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    SYMVER(rule = Rule.dirNameRule("symver")),
    TAG(rule = Rule {
        Seq(
            Specific(".tag", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    TEXT(rule = Rule {
        Seq(
            Specific(".text", ignoreCase = true),
            Optional {
                InSpecific(AsmTokenType.SYMBOL)
            }
        )
    }),
    TITLE(rule = Rule {
        Seq(
            Specific(".title", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    TLS_COMMON(rule = Rule {
        Seq(
            Specific(".tls_common", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR),
            Optional {
                Seq(
                    Specific(","),
                    SpecNode(ASNodeType.INT_EXPR)
                )
            }
        )
    }),
    TYPE(rule = Rule.dirNameRule("type")),
    ULEB128(rule = Rule {
        Seq(
            Specific(".uleb128", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    VAL(rule = Rule {
        Seq(
            Specific(".val", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    VERSION(rule = Rule {
        Seq(
            Specific(".version", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )
    }),
    VTABLE_ENTRY(rule = Rule {
        Seq(
            Specific(".vtable_entry", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    VTABLE_INHERIT(rule = Rule {
        Seq(
            Specific(".vtable_inherit", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    WARNING(rule = Rule {
        Seq(
            Specific(".warning", ignoreCase = true),
            SpecNode(ASNodeType.STRING_EXPR)
        )

    }),
    WEAK(rule = Rule {
        Seq(
            Specific(".weak", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Repeatable {
                Seq(Specific(","), InSpecific(AsmTokenType.SYMBOL))
            })
    }),
    WEAKREF(rule = Rule {
        Seq(
            Specific(".weakref", ignoreCase = true),
            InSpecific(AsmTokenType.SYMBOL),
            Specific(","),
            InSpecific(AsmTokenType.SYMBOL)
        )
    }),
    WORD(rule = Rule {
        Seq(
            Specific(".word", ignoreCase = true),
            Optional {
                Seq(SpecNode(ASNodeType.INT_EXPR), Repeatable {
                    Seq(Specific(","), SpecNode(ASNodeType.INT_EXPR))
                })
            }
        )
    }),
    ZERO(rule = Rule {
        Seq(
            Specific(".zero", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR)
        )
    }),
    _2BYTE(rule = Rule {
        Seq(
            Specific(".2byte", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(ASNodeType.INT_EXPR)
                )
            }
        )
    }),
    _4BYTE(rule = Rule {
        Seq(
            Specific(".4byte", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(ASNodeType.INT_EXPR)
                )
            }
        )
    }),
    _8BYTE(rule = Rule {
        Seq(
            Specific(".8byte", ignoreCase = true),
            SpecNode(ASNodeType.INT_EXPR),
            Repeatable {
                Seq(
                    Specific(","),
                    SpecNode(ASNodeType.INT_EXPR)
                )
            }
        )
    }),
    /*INSERTION(rule = Rule {
        Seq(
            InSpecific(AsmTokenType.SYMBOL),
            Optional {
                Seq(
                    SpecNode(GASNodeType.ARG_DEF),
                    Repeatable {
                        Seq(
                            Specific(","),
                            SpecNode(GASNodeType.ARG_DEF),
                        )
                    }
                )
            },
        )
    })*/;

    override val typeName: String
        get() = name

    override fun getDetectionString(): String = if (!this.contentStartsDirectly) this.name.removePrefix("_") else ""

    override fun buildDirectiveContent(lexer: AsmLexer, targetSpec: TargetSpec<*>): ASNode.Directive? {
        val initialPos = lexer.position
        val result = this.rule?.matchStart(lexer, targetSpec)

        if (result == null) {
            lexer.position = initialPos
            return null
        }

        if (result.matches) {
            //nativeLog("RuleResult: ${result} for $this")
            val identificationToken = result.matchingTokens.firstOrNull { it.type == AsmTokenType.DIRECTIVE }
            return if (identificationToken != null) {
                ASNode.Directive(
                    this,
                    identificationToken,
                    result.matchingTokens - identificationToken,
                    result.matchingNodes
                )
            } else {
                ASNode.Directive(this, identificationToken, result.matchingTokens, result.matchingNodes)
            }
        }

        lexer.position = initialPos
        return null
    }

    override suspend fun build(builder: AsmCodeGenerator<*>, dir: ASNode.Directive) {
        /**
         * Check Semantic
         */
        when (this) {
            BYTE -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(8)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 8 bits."))
                }
            }

            HWORD -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(16)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 16 bits."))
                }
            }

            INT -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(32)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 32 bits."))
                }
            }

            SHORT -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(16)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 16 bits."))
                }
            }

            WORD -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(32)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 32 bits."))
                }
            }

            _2BYTE -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(16)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 16 bits."))
                }
            }

            _4BYTE -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(32)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 32 bits."))
                }
            }

            _8BYTE -> dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>().forEach {
                val value = it.evaluate(builder)
                if (!value.fitsInSignedOrUnsigned(64)) {
                    it.annotations.add(Annotation.error(it, "$value exceeds 64 bits."))
                }
            }

            else -> {}
        }

        /**
         * Execute Directive
         */
        when (this) {
            ALIGN -> TODO()
            ASCII -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.StringExpr>()
                exprs.forEach {
                    builder.currentSection.content.putBytes(it.evaluate(true).encodeToByteArray())
                }
            }

            ASCIZ -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.StringExpr>()
                exprs.forEach {
                    builder.currentSection.content.putBytes(it.evaluate(true).encodeToByteArray() + 0)
                }
            }

            ATTACH_TO_GROUP_NAME -> TODO()
            BALIGN -> TODO()
            BALIGNL -> TODO()
            BALIGNW -> TODO()
            BSS -> builder.getOrCreateSectionAndSetCurrent(".bss", Shdr.SHT_bss, Shdr.SHF_bss.toUInt64())
            BYTE -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(8)) {
                        expr.addError("$eval exceeds 8 bits")
                    }

                    builder.currentSection.content.put(eval.toInt8())
                }
            }

            COMM -> TODO()
            DATA -> builder.getOrCreateSectionAndSetCurrent(".data", Shdr.SHT_data, Shdr.SHF_data.toUInt64())
            DEF -> TODO()
            DESC -> TODO()
            DIM -> TODO()
            DOUBLE -> TODO()
            EJECT -> TODO()
            EQU -> {
                val identifier = dir.allTokens.firstOrNull { it.type == AsmTokenType.SYMBOL }?.value
                if (identifier == null) {
                    dir.addError("Identifier is missing!")
                    return
                }

                val expr = dir.additionalNodes.firstOrNull()

                if (expr == null) {
                    dir.addError("Expression is missing!")
                    return
                }

                when (expr) {
                    is ASNode.NumericExpr -> {
                        val evaluated = expr.evaluate(builder)
                        try {
                            builder.getOrCreateAbsSymbolInCurrentSection(identifier, evaluated)
                        } catch (e: Exception) {
                            dir.addError(e.message.toString())
                        }
                    }

                    else -> dir.addError("equ only supporting numeric expressions.")
                }
            }

            EQUIV -> TODO()
            EQV -> TODO()
            ERR -> TODO()
            ERROR -> TODO()
            EXITM -> TODO()
            EXTERN -> TODO()
            FAIL -> TODO()
            FILE -> TODO()
            FILL -> TODO()
            FLOAT -> TODO()
            FUNC -> TODO()
            GLOBAL -> {
                val identifier = dir.allTokens.last { it.type == AsmTokenType.SYMBOL }.value

                if (builder.symbols.find { it.name == identifier } == null) {
                    builder.getOrCreateAbsSymbolInCurrentSection(identifier, BigInt.ZERO)
                }

                builder.symbols.filter { it.name == identifier }.forEach {
                    it.binding = AsmCodeGenerator.Symbol.Binding.GLOBAL
                }
            }

            GLOBL -> {
                val identifier = dir.allTokens.last { it.type == AsmTokenType.SYMBOL }.value

                if (builder.symbols.find { it.name == identifier } == null) {
                    builder.getOrCreateAbsSymbolInCurrentSection(identifier, BigInt.ZERO)
                }

                builder.symbols.filter { it.name == identifier }.forEach {
                    it.binding = AsmCodeGenerator.Symbol.Binding.GLOBAL
                }
            }

            GNU_ATTRIBUTE -> TODO()
            HIDDEN -> TODO()
            HWORD -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(16)) {
                        expr.addError("$eval exceeds 16 bits")
                    }

                    builder.currentSection.content.put(eval.toInt16())
                }
            }

            IDENT -> TODO()
            INCBIN -> TODO()
            INCLUDE -> {
                val fileName = dir.additionalNodes.filterIsInstance<ASNode.StringExpr>()[0].evaluate(true)

                if (fileName.isEmpty()) {
                    dir.addError("expected a file name!")
                }

                val fpath = FPath.of(builder.psiManager.vfs, *fileName.split("/").toTypedArray())

                val asmFile = builder.psiManager.findAndUpdate(fpath)

                if (asmFile !is AsmFile) {
                    dir.addError("Couldn't find include $fpath!")
                    return
                }

                asmFile.program.getAllStatements().forEach {
                    builder.execute(it)
                }
            }

            INT -> TODO()
            INTERNAL -> TODO()
            IRP -> TODO()
            IRPC -> TODO()
            LCOMM -> TODO()
            LFLAGS -> TODO()
            LINE -> TODO()
            LINKONCE -> TODO()
            LIST -> TODO()
            LN -> TODO()
            LOC -> TODO()
            LOC_MARK_LABELS -> TODO()
            LOCAL -> TODO()
            LONG -> TODO()
            MACRO -> TODO()
            MRI -> TODO()
            NOALTMACRO -> TODO()
            NOLIST -> TODO()
            NOP -> TODO()
            NOPS -> TODO()
            OCTA -> TODO()
            OFFSET -> TODO()
            ORG -> TODO()
            P2ALIGN -> TODO()
            P2ALIGNW -> TODO()
            P2ALIGNL -> TODO()
            POPSECTION -> TODO()
            PREVIOUS -> TODO()
            PRINT -> TODO()
            PROTECTED -> TODO()
            PSIZE -> TODO()
            PURGEM -> TODO()
            PUSHSECTION -> TODO()
            QUAD -> TODO()
            RODATA -> builder.getOrCreateSectionAndSetCurrent(".rodata", Shdr.SHT_rodata, Shdr.SHF_rodata.toUInt64())
            RELOC -> TODO()
            REPT -> TODO()
            SBTTL -> TODO()
            SCL -> TODO()
            SECTION -> TODO()
            SET_ALT -> TODO()
            SET -> TODO()
            SHORT -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(16)) {
                        expr.addError("$eval exceeds 16 bits")
                    }

                    builder.currentSection.content.put(eval.toInt16())
                }
            }

            SINGLE -> TODO()
            SIZE -> TODO()
            SKIP -> TODO()
            SLEB128 -> TODO()
            SPACE -> TODO()
            STABD -> TODO()
            STABN -> TODO()
            STABS -> TODO()
            STRING -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.StringExpr>()

                for (expr in exprs) {
                    try {
                        val str = expr.evaluate(true).encodeToByteArray() + 0b0
                        builder.currentSection.content.putBytes(str)
                    } catch (e: Exception) {
                        dir.addError("Couldn't evaluate string expression!")
                    }
                }

            }

            STRING8 -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.StringExpr>()

                for (expr in exprs) {
                    try {
                        val str = expr.evaluate(true).encodeToByteArray() + 0b0
                        builder.currentSection.content.putBytes(str)
                    } catch (e: Exception) {
                        dir.addError("Couldn't evaluate string expression!")
                    }
                }
            }

            STRING16 -> TODO()
            STRING32 -> TODO()
            STRUCT -> TODO()
            SUBSECTION -> TODO()
            SYMVER -> TODO()
            TAG -> TODO()
            TEXT -> builder.getOrCreateSectionAndSetCurrent(".text", Shdr.SHT_text, Shdr.SHF_text.toUInt64())
            TITLE -> TODO()
            TLS_COMMON -> TODO()
            TYPE -> TODO()
            ULEB128 -> TODO()
            VAL -> TODO()
            VERSION -> TODO()
            VTABLE_ENTRY -> TODO()
            VTABLE_INHERIT -> TODO()
            WARNING -> TODO()
            WEAK -> TODO()
            WEAKREF -> TODO()
            WORD -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val int32 = expr.evaluate(builder)

                    if (!int32.fitsInSignedOrUnsigned(32)) {
                        expr.addError("$int32 exceeds 32 bits")
                    }

                    builder.currentSection.content.put(int32.toInt32())
                }
            }

            ZERO -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()

                val amountExpr = exprs[0]
                val amount = amountExpr.evaluate(builder).toInt()

                builder.currentSection.content.pad(amount)
            }

            _2BYTE -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(16)) {
                        expr.addError("$eval exceeds 16 bits")
                    }

                    builder.currentSection.content.put(eval.toUInt16())
                }
            }

            _4BYTE -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(32)) {
                        expr.addError("$eval exceeds 32 bits")
                    }

                    builder.currentSection.content.put(eval.toUInt16())
                }
            }

            _8BYTE -> {
                val exprs = dir.additionalNodes.filterIsInstance<ASNode.NumericExpr>()
                for (expr in exprs) {
                    val eval = expr.evaluate(builder)

                    if (!eval.fitsInSignedOrUnsigned(64)) {
                        expr.addError("$eval exceeds 64 bits")
                    }

                    builder.currentSection.content.put(eval.toUInt16())
                }
            }

            else -> dir.annotations.add(Annotation.warn(dir, "Not $this yet implemented."))
        }
    }
}