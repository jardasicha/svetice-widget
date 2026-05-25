package cz.svetice.widget

data class Train(
    val planned: String,   // "06:12"
    val delayMin: Int      // 0 = on time
)
