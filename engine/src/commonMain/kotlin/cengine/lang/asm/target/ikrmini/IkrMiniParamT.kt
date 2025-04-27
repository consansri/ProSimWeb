package cengine.lang.asm.target.ikrmini

enum class IkrMiniParamT(val size: Int, val example: String) {
    IND( 2, "(([16 Bit]))"),
    IND_OFF(3, "([16 Bit],([16 Bit]))"),
    DIR( 2, "([16 Bit])"),
    IMM( 2, "#[16 Bit]"),
    DEST(2, "[label]"),
    IMPL(1, "");

}