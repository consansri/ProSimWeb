package views

import AppLogic
import StyleConst
import emotion.react.css
import extendable.components.connected.FileBuilder
import extendable.components.connected.FileHandler
import js.core.asList

import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.header
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.nav
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import tools.DebugTools
import web.buffer.Blob
import web.cssom.*
import web.storage.localStorage
import web.timers.*
import web.html.*
import web.file.*
import web.dom.*
import web.location.location
import web.url.URL


external interface MenuProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit
}

val Menu = FC<MenuProps>() { props ->

    val data by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val (navHidden, setNavHidden) = useState(true)
    val (archsHidden, setArchsHidden) = useState(true)

    val (importHidden, setImportHidden) = useState(true)

    val (exportHidden, setExportHidden) = useState(true)
    val (selFormat, setSelFormat) = useState<FileBuilder.ExportFormat>(FileBuilder.ExportFormat.entries.first())

    val (selAddrW, setSelAddrW) = useState<Int>(data.getArch().getMemory().getAddressSize().bitWidth)
    val (selDataW, setSelDataW) = useState<Int>(data.getArch().getMemory().getWordSize().bitWidth)

    val navRef = useRef<HTMLElement>()
    val archsRef = useRef<HTMLDivElement>()
    val importRef = useRef<HTMLInputElement>()

    val downloadAsyncRef = useRef<Timeout>()

    fun showNavbar(state: Boolean) {
        navRef.current?.let {
            if (state) {
                it.classList.add(StyleConst.Header.CLASS_MOBILE_OPEN)
            } else {
                it.classList.remove(StyleConst.Header.CLASS_MOBILE_OPEN)
            }
            setNavHidden(!state)
        }
    }

    fun showArchs(state: Boolean) {
        archsRef.current?.let {
            if (state) {
                it.classList.add("nav-dropdown-open")
            } else {
                it.classList.remove("nav-dropdown-open")
            }
            setArchsHidden(!state)
        }
    }

    fun importFile(file: dynamic) {
        val reader = FileReader()
        reader.readAsText(file as Blob, "UTF-8")

        reader.onloadend = {
            console.log("read ${reader.result}")
            data.getArch().getFileHandler().import(FileHandler.File(file.name as String, reader.result as String))
        }
    }

    header {
        css {
            backgroundColor = StyleConst.Header.BgColor.get()
            color = StyleConst.Header.FgColor.get()

            a {
                color = StyleConst.Header.FgColor.get()
            }
            nav {
                backgroundColor = StyleConst.Header.BgColor.get()
            }
        }

        h3 {
            +"ProSimWeb"
        }

        nav {
            ref = navRef
            a {
                href = "#home"
                onClick = {
                    console.log("#home clicked")
                }

                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.home

                }
            }

            a {
                href = "#"
                onClick = {
                    showArchs(true)
                }
                img {
                    className = ClassName("nav-img")
                    src = StyleConst.Icons.processor
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Upload"
                    src = StyleConst.Icons.import
                }

                onClick = {
                    setImportHidden(!importHidden)
                    setExportHidden(true)
                }
            }

            a {
                href = "#"
                img {
                    className = ClassName("nav-img")
                    alt = "Download"
                    src = StyleConst.Icons.export
                }

                onClick = {
                    setExportHidden(!exportHidden)
                    setImportHidden(true)
                }
            }

            button {
                className = ClassName("nav-btn nav-close-btn")
                title = "close nav"
                onClick = {
                    showNavbar(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"

                }
            }
        }

        button {
            className = ClassName("nav-btn")
            title = "open nav"

            onClick = {
                showNavbar(true)
            }

            img {
                className = ClassName("nav-img")
                src = "icons/bars.svg"

            }
        }

        div {
            ref = archsRef

            css(ClassName(StyleConst.Header.CLASS_DROPDOWN)) {
                if (archsHidden) {
                    visibility = Visibility.hidden
                    transform = translatey(-100.vh)
                } else {
                    visibility = Visibility.visible
                    transform = translatey(0.vh)
                }
            }

            for (id in data.getArchList().indices) {
                a {
                    href = "#${data.getArchList()[id].getName()}"

                    onClick = { event ->
                        showArchs(false)
                        val newData = data
                        newData.selID = id
                        localStorage.setItem(StorageKey.ARCH_TYPE, "$id")
                        console.log("Load " + data.getArch().getName())
                        event.currentTarget.classList.toggle("nav-arch-active")
                        //updateParent(newData)
                        location.reload()
                    }

                    +data.getArchList()[id].getName()
                }
            }

            a {
                onClick = {
                    showArchs(false)
                }

                img {
                    className = ClassName("nav-img")
                    src = "icons/times.svg"
                }
            }
        }

        if (!exportHidden) {
            div {
                className = ClassName(StyleConst.Header.CLASS_OVERLAY)

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/cancel.svg"
                    }
                    onClick = {
                        setExportHidden(true)
                    }
                }

                select {

                    defaultValue = selFormat.name

                    option {
                        disabled = true
                        value = ""
                        +"Choose Export Format"
                    }

                    for (format in FileBuilder.ExportFormat.entries) {
                        option {
                            value = format.name
                            +format.uiName
                        }
                    }

                    onChange = {
                        for (format in FileBuilder.ExportFormat.entries) {
                            if (format.name == it.currentTarget.value) {
                                setSelFormat(format)
                                break
                            }
                        }
                    }
                }

                if (selFormat != FileBuilder.ExportFormat.CURRENT_FILE) {
                    div {
                        className = ClassName(StyleConst.Header.CLASS_OVERLAY_LABELEDINPUT)
                        label {
                            htmlFor = "vhdlAddrInput"
                            +"Address Width [Bits]"
                        }

                        input {
                            id = "vhdlAddrInput"
                            type = InputType.number
                            min = 1.0
                            max = 2048.0
                            defaultValue = selAddrW.toString()

                            onChange = {
                                setSelAddrW(it.currentTarget.valueAsNumber.toInt())
                            }
                        }
                    }

                    div {
                        className = ClassName(StyleConst.Header.CLASS_OVERLAY_LABELEDINPUT)
                        label {
                            htmlFor = "vhdlDataInput"
                            +"Data Width [Bits]"
                        }

                        input {
                            id = "vhdlDataInput"
                            type = InputType.number
                            min = 1.0
                            max = 2048.0
                            defaultValue = selDataW.toString()

                            onChange = {
                                setSelDataW(it.currentTarget.valueAsNumber.toInt())
                            }
                        }
                    }
                }

                a {
                    img {
                        className = ClassName("nav-img")
                        src = "icons/download.svg"
                    }

                    onClick = {
                        downloadAsyncRef.current?.let {
                            clearInterval(it)
                        }

                        downloadAsyncRef.current = setTimeout({
                            val blob = data.getArch().getFormattedFile(selFormat, FileBuilder.Setting.DataWidth(selDataW), FileBuilder.Setting.AddressWidth(selAddrW))
                            val anchor = document.createElement("a") as HTMLAnchorElement
                            anchor.href = URL.createObjectURL(blob)
                            anchor.style.display = "none"
                            document.body.appendChild(anchor)
                            if (selFormat.ending.isNotEmpty()) {
                                anchor.download = data.getArch().getFileHandler().getCurrNameWithoutType() + selFormat.ending
                            } else {
                                anchor.download = data.getArch().getFileHandler().getCurrent().getName()
                            }
                            anchor.click()
                        }, 10)

                        setTimeout({
                            downloadAsyncRef.current?.let {
                                clearInterval(it)
                                console.warn("Download File Generation took to long!")
                            }
                        }, 3000)

                        setExportHidden(true)
                    }
                }

            }
        }

        if (!importHidden) {
            div {
                css {
                    position = Position.fixed
                    bottom = 0.px
                    left = 0.px
                    width = 100.vw
                    zIndex = integer(1000)
                    padding = 1.rem
                    backgroundColor = Color("#5767aa")

                    display = Display.flex
                    justifyContent = JustifyContent.center
                    gap = 2.rem
                    alignContent = AlignContent.spaceEvenly
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/cancel.svg"
                    }
                    onClick = {
                        setImportHidden(true)
                    }
                }

                input {
                    ref = importRef
                    type = InputType.file
                    multiple = true
                }

                a {

                    img {
                        className = ClassName("nav-img")
                        src = "icons/upload.svg"
                    }
                    onClick = {
                        val files = importRef.current?.files?.asList() ?: emptyList<File>()

                        if (!files.isEmpty()) {
                            for (file in files) {
                                importFile(file)
                            }
                        }
                        props.updateParent()
                        setImportHidden(true)
                    }
                }

            }
        }
    }

    useEffect(selFormat, selAddrW, selDataW) {
        // generate downloadable file

    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) Menu")
        }
    }

}