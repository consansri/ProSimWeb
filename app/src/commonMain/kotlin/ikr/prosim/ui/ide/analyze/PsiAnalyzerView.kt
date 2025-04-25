package ikr.prosim.ui.ide.analyze

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import cengine.editor.annotation.Severity.*
import cengine.psi.PsiManager
import cengine.psi.core.PsiService
import cengine.psi.elements.PsiFile
import uilib.interactable.CButton
import uilib.label.CLabel
import uilib.layout.TabItem
import uilib.layout.TabbedPane
import uilib.layout.VerticalToolBar
import uilib.UIState

@Composable
fun PsiAnalyzerView(
    psiManager: PsiManager<*>,
    onOpen: (PsiFile, index: Int) -> Unit,
) {

    val theme = UIState.Theme.value
    UIState.Scale.value
    val icons = UIState.Icon.value

    val tabs = psiManager.psiCache.map { TabItem(it, icons.file, it.key.toString()) }

    TabbedPane(tabs, modifier = Modifier.fillMaxSize(), content = { tabIndex ->

        key(tabs[tabIndex].value.value.annotations) {
            val (_, psiFile) = tabs[tabIndex].value
            var annotations by remember {
                mutableStateOf(PsiService.annotationsMapped(psiFile).toList().sortedBy { it.first.range.first }.flatMap {
                    val psiPath = PsiService.path(it.first).joinToString(" > ") { element -> element.type.typeName }
                    it.second.map { annotation -> psiPath to annotation }
                })
            }

            Row(Modifier.fillMaxSize()) {
                VerticalToolBar(
                    upper = {
                        if (annotations.any { it.second.severity == ERROR }) {
                            CLabel(
                                icon = icons.statusError,
                                iconTint = theme.COLOR_RED
                            )
                        } else {
                            CLabel(
                                icon = icons.statusFine,
                                iconTint = theme.COLOR_GREEN
                            )
                        }

                        CButton({
                            psiManager.queueUpdate(psiFile.file) {
                                annotations = PsiService.annotationsMapped(psiFile).toList().sortedBy { it.first.range.first }.flatMap {
                                    val psiPath = PsiService.path(it.first).joinToString(" > ") { element -> element.type.typeName }
                                    it.second.map { annotation -> psiPath to annotation }
                                }
                            }

                        }, icon = icons.refresh)
                    },
                    lower = {

                    }
                )

                if (annotations.isNotEmpty()) {
                    LazyColumn(Modifier.fillMaxWidth()) {

                        items(annotations, key = {
                            it
                        }) { (psiPath, annotation) ->
                            val iconColor = when (annotation.severity) {
                                INFO -> theme.COLOR_GREEN
                                WARNING -> theme.COLOR_YELLOW
                                ERROR -> theme.COLOR_RED
                            }
                            val icon = when (annotation.severity) {
                                INFO -> icons.statusFine
                                WARNING -> icons.info
                                ERROR -> icons.statusError
                            }

                            CButton(onClick = {
                                onOpen(psiFile, annotation.range.first)
                            }, Modifier.fillMaxWidth(), Arrangement.Start, icon = icon, textStyle = UIState.CodeStyle.current, textAlign = TextAlign.Left, iconTint = iconColor, text = "$psiPath: ${annotation.message}", tooltip = annotation.location(psiFile))
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CLabel(text = "No Issues found!")
                    }
                }

            }
        }
    })

}