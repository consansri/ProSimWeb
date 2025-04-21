
import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement


object JSTools {

    fun downloadFile(path: String, fileName: String) {
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = "$path$fileName"
        anchor.download = fileName
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
    }


}