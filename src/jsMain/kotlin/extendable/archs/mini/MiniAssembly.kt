package extendable.archs.mini

import extendable.Architecture
import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class MiniAssembly: Assembly() {
    override fun generateTranscript(architecture: Architecture, grammarTree: Grammar.GrammarTree) {
        TODO("Not yet implemented")
    }

    override fun generateByteCode(architecture: Architecture, grammarTree: Grammar.GrammarTree): AssemblyMap {
        TODO("Not yet implemented")
    }
}