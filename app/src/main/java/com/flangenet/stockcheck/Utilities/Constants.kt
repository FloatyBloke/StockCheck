package com.flangenet.stockcheck.Utilities

import java.text.SimpleDateFormat

const val EXTRA_CHECKLIST_TYPE = "checklistType"
const val EXTRA_CHECKLIST_DATE = "checklistDate"
val dateFormat = SimpleDateFormat("dd/MM/yyyy")
val prettyDateFormat = SimpleDateFormat("EEEE MMM d y")
val sqlDateFormat = SimpleDateFormat("yyyy-MM-dd")