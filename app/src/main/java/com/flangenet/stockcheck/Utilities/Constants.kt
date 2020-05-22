package com.flangenet.stockcheck.Utilities

import java.text.SimpleDateFormat

const val EXTRA_CHECKLIST_TYPE = "checklistType"
const val EXTRA_CHECKLIST_DESC = "checklistDesc"
const val EXTRA_CHECKLIST_DATE = "checklistDate"
const val EXTRA_CHECK_ARRAY = "checklistArray"
const val PASS_ME_A_STOCK_CHECK = 337
val dateFormat = SimpleDateFormat("dd/MM/yyyy")
val prettyDateFormat = SimpleDateFormat("EEEE MMM d y")
val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd")