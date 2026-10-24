package com.example.core

import com.example.data.local.entity.ExamQuestion
import org.json.JSONArray
import org.json.JSONObject

object ExamQuestionBank {

    fun serializeQuestions(questions: List<ExamQuestion>): String {
        val array = JSONArray()
        for (q in questions) {
            val obj = JSONObject()
            obj.put("id", q.id)
            obj.put("question", q.question)
            val optArray = JSONArray()
            for (opt in q.options) {
                optArray.put(opt)
            }
            obj.put("options", optArray)
            obj.put("correctOptionIndex", q.correctOptionIndex)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeQuestions(jsonStr: String): List<ExamQuestion> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<ExamQuestion>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optInt("id", i + 1)
                val question = obj.optString("question", "")
                val optArray = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optArray != null) {
                    for (j in 0 until optArray.length()) {
                        options.add(optArray.getString(j))
                    }
                }
                while (options.size < 4) {
                    options.add("Option ${('A' + options.size)}")
                }
                val correctOptionIndex = obj.optInt("correctOptionIndex", 0)
                list.add(ExamQuestion(id, question, options, correctOptionIndex))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun serializeAllowedStudentIds(studentIds: List<String>): String {
        if (studentIds.isEmpty()) return "ALL"
        val arr = JSONArray()
        for (id in studentIds) {
            arr.put(id)
        }
        return arr.toString()
    }

    fun deserializeAllowedStudentIds(jsonStr: String): List<String> {
        if (jsonStr.isBlank() || jsonStr == "ALL") return emptyList()
        val list = mutableListOf<String>()
        try {
            if (jsonStr.trim().startsWith("[")) {
                val arr = JSONArray(jsonStr)
                for (i in 0 until arr.length()) {
                    list.add(arr.getString(i))
                }
            } else {
                list.addAll(jsonStr.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun deserializeAnswers(jsonStr: String): Map<Int, Int> {
        if (jsonStr.isBlank()) return emptyMap()
        val map = mutableMapOf<Int, Int>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val qIdx = key.toIntOrNull()
                if (qIdx != null) {
                    map[qIdx] = obj.optInt(key, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun serializeAnswers(answers: Map<Int, Int>): String {
        val obj = JSONObject()
        for ((k, v) in answers) {
            obj.put(k.toString(), v)
        }
        return obj.toString()
    }

    fun getDefault50Questions(courseName: String = "Advance Diploma in Computer Applications (ADCA)"): List<ExamQuestion> {
        val questions = listOf(
            // 1-5: Computer Fundamentals
            ExamQuestion(1, "What component of the computer is known as the 'Brain' of the system?", listOf("Central Processing Unit (CPU)", "Arithmetic Logic Unit", "Random Access Memory", "Hard Disk Drive"), 0),
            ExamQuestion(2, "Which type of memory is volatile and loses its data when power is turned off?", listOf("Read Only Memory (ROM)", "Flash Storage", "Random Access Memory (RAM)", "Magnetic Tape"), 2),
            ExamQuestion(3, "1 Gigabyte (GB) is equivalent to exactly how many Megabytes (MB)?", listOf("1000 MB", "1024 MB", "512 MB", "2048 MB"), 1),
            ExamQuestion(4, "Which bus architecture connects the CPU directly to high-speed cache and RAM?", listOf("Expansion Bus", "System Bus", "Serial Bus", "Universal Serial Bus"), 1),
            ExamQuestion(5, "What is the primary firmware executed immediately upon powering on a computer?", listOf("Device Driver", "Operating Kernel", "BIOS / UEFI", "Bootloader Utility"), 2),

            // 6-10: Operating Systems & GUI
            ExamQuestion(6, "Which command in Windows Command Prompt displays the complete IP network configuration?", listOf("ipconfig /all", "netstat -a", "ping localhost", "traceroute"), 0),
            ExamQuestion(7, "Which open-source operating system kernel was created by Linus Torvalds in 1991?", listOf("FreeBSD", "Linux", "Minix", "Solaris"), 1),
            ExamQuestion(8, "What file system is the modern default for Windows operating system installations?", listOf("FAT32", "exFAT", "NTFS", "ext4"), 2),
            ExamQuestion(9, "Which Windows shortcut instantly locks the desktop screen?", listOf("Ctrl + Shift + Esc", "Win + L", "Alt + F4", "Win + D"), 1),
            ExamQuestion(10, "In OS architecture, what is the term for the core software managing memory and CPU hardware?", listOf("Shell", "Kernel", "Compiler", "Assembler"), 1),

            // 11-15: MS Word & Document Processing
            ExamQuestion(11, "Which MS Word feature enables generating customized letters or certificates for multiple recipients?", listOf("AutoCorrect", "Mail Merge", "Macro Scripting", "Track Changes"), 1),
            ExamQuestion(12, "What is the default file extension for documents saved in modern Microsoft Word?", listOf(".doc", ".docx", ".rtf", ".txt"), 1),
            ExamQuestion(13, "Which shortcut key in MS Word is used to center-align the selected paragraph?", listOf("Ctrl + C", "Ctrl + J", "Ctrl + E", "Ctrl + R"), 2),
            ExamQuestion(14, "Which view in MS Word shows how the document will appear when printed on paper?", listOf("Web Layout", "Draft View", "Print Layout", "Outline View"), 2),
            ExamQuestion(15, "In MS Word, what is the keyboard shortcut for replacing text throughout a document?", listOf("Ctrl + F", "Ctrl + H", "Ctrl + G", "Ctrl + K"), 1),

            // 16-20: MS Excel & Data Analysis
            ExamQuestion(16, "Every formula in Microsoft Excel must strictly begin with which symbol?", listOf("@", "=", "+", "#"), 1),
            ExamQuestion(17, "Which Excel formula searches for a value in the first column and retrieves data from a corresponding column?", listOf("HLOOKUP", "VLOOKUP", "MATCH", "SEARCH"), 1),
            ExamQuestion(18, "Which formula sums values only if they meet a specific specified condition?", listOf("SUMIF", "COUNTIF", "TOTALIF", "AVERAGEIF"), 0),
            ExamQuestion(19, "What tool in MS Excel is used to summarize, analyze, explore, and present large tabular datasets?", listOf("Pivot Table", "Format Painter", "Goal Seek", "Data Sorter"), 0),
            ExamQuestion(20, "What cell reference format (\$A\$1) represents an absolute reference in Excel?", listOf("Relative Reference", "Absolute Reference", "Mixed Reference", "Dynamic Range"), 1),

            // 21-25: MS PowerPoint & Presentation Graphics
            ExamQuestion(21, "Which shortcut key immediately starts a slide show from the very first slide in PowerPoint?", listOf("F5", "Shift + F5", "Ctrl + F5", "F7"), 0),
            ExamQuestion(22, "What feature in PowerPoint controls universal font styles, logos, and placeholders across all slides?", listOf("Slide Sorter", "Slide Master", "Animation Pane", "Design Ideas"), 1),
            ExamQuestion(23, "What is the term for visual motion effects applied when moving from one slide to the next?", listOf("Custom Animation", "Slide Transition", "Motion Path", "Timing Trigger"), 1),
            ExamQuestion(24, "Which view displays all slides as compact thumbnails for rapid reordering?", listOf("Slide Sorter View", "Normal View", "Reading View", "Slide Show"), 0),
            ExamQuestion(25, "Which shortcut duplicate the current selected slide in PowerPoint?", listOf("Ctrl + N", "Ctrl + D", "Ctrl + M", "Ctrl + Shift + D"), 1),

            // 26-30: Tally Prime & Accounting Principles
            ExamQuestion(26, "Which function key in Tally Prime is used to record a Contra Voucher (Cash/Bank transfer)?", listOf("F4", "F5", "F6", "F7"), 0),
            ExamQuestion(27, "Which voucher in Tally Prime is used when paying expenses or settling vendor accounts?", listOf("Payment Voucher (F5)", "Receipt Voucher (F6)", "Journal Voucher (F7)", "Sales Voucher (F8)"), 0),
            ExamQuestion(28, "According to Golden Rules of Accounting, what is the rule for Real Accounts?", listOf("Debit what comes in, Credit what goes out", "Debit the receiver, Credit the giver", "Debit all expenses, Credit all incomes", "Debit assets, Credit liabilities"), 0),
            ExamQuestion(29, "Which report in Tally Prime shows arithmetic equality of debits and credits before financial statements?", listOf("Balance Sheet", "Profit & Loss A/c", "Trial Balance", "Day Book"), 2),
            ExamQuestion(30, "Which voucher in Tally is used for adjusting entries and non-cash depreciation transactions?", listOf("F4", "F5", "F7 (Journal)", "F9"), 2),

            // 31-35: GST & Commercial Taxation
            ExamQuestion(31, "What does the abbreviation 'GST' stand for in modern commerce?", listOf("General Sales Tax", "Goods and Services Tax", "Government State Tax", "Gross Standard Tariff"), 1),
            ExamQuestion(32, "Which two tax components apply to an intra-state sale within the same state?", listOf("CGST and SGST", "IGST and CGST", "UTGST and IGST", "Customs and Excise"), 0),
            ExamQuestion(33, "Which tax is levied exclusively on inter-state supply of goods and services?", listOf("Integrated GST (IGST)", "Central GST (CGST)", "State GST (SGST)", "Municipal Tax"), 0),
            ExamQuestion(34, "What does the 8-digit HSN code stand for in invoice billing?", listOf("Harmonized System of Nomenclature", "High Standard Number", "Home Sales Notation", "Hierarchical Series Number"), 0),
            ExamQuestion(35, "What is the electronic document required under GST for transporting goods worth over ₹50,000?", listOf("E-Way Bill", "Consignment Note", "Delivery Challan", "Tax Debit Note"), 0),

            // 36-40: Python Programming
            ExamQuestion(36, "Which keyword is used to declare a function definition in Python?", listOf("func", "def", "function", "lambda"), 1),
            ExamQuestion(37, "What is the output data type of `x = [10, 20, 30]` in Python?", listOf("tuple", "set", "list", "dict"), 2),
            ExamQuestion(38, "Which built-in Python function returns the total number of items in a sequence?", listOf("count()", "size()", "length()", "len()"), 3),
            ExamQuestion(39, "How are code blocks (like loops and conditions) syntactically structured in Python?", listOf("Curly braces {}", "Indentation (whitespace)", "Begin/End keywords", "Square brackets []"), 1),
            ExamQuestion(40, "Which operator is used to perform integer floor division in Python?", listOf("/", "//", "%", "**"), 1),

            // 41-45: Web Technology & Internet
            ExamQuestion(41, "What does HTML stand for in website authoring?", listOf("HyperText Markup Language", "Hyperlink Text Management List", "High Tech Modern Layout", "HyperTransfer Machine Language"), 0),
            ExamQuestion(42, "Which HTML tag is used to define an internal stylesheet block?", listOf("<css>", "<script>", "<style>", "<link>"), 2),
            ExamQuestion(43, "Which CSS property is used to create space inside a container's border?", listOf("margin", "padding", "spacing", "outline"), 1),
            ExamQuestion(44, "Which JavaScript method displays an interactive modal message box to the user?", listOf("alert()", "console.log()", "document.write()", "print()"), 0),
            ExamQuestion(45, "Which HTTP status code indicates a successful resource retrieval?", listOf("200 OK", "301 Moved", "404 Not Found", "500 Internal Error"), 0),

            // 46-50: Databases & Cybersecurity
            ExamQuestion(46, "What does SQL stand for in database architecture?", listOf("Structured Query Language", "Standard Quality Logic", "Systematic Queue Lookup", "Sequential Query Library"), 0),
            ExamQuestion(47, "Which SQL command is used to retrieve data records from a database table?", listOf("FETCH", "SELECT", "GET", "EXTRACT"), 1),
            ExamQuestion(48, "Which attribute uniquely identifies each record in a relational database table?", listOf("Foreign Key", "Index Pointer", "Primary Key", "Composite Column"), 2),
            ExamQuestion(49, "What security attack tricks users into revealing passwords via deceptive emails or fake websites?", listOf("Phishing", "DDoS", "Trojan horse", "Buffer overflow"), 0),
            ExamQuestion(50, "What network security hardware or software monitors and filters incoming and outgoing traffic?", listOf("Firewall", "Router", "Repeater", "Switch Hub"), 0)
        )
        return questions
    }
}
