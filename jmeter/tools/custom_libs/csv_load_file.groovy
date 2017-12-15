/**
 * Helper script to load csv file to memory.
 *
 * Because this data will be stored separately for each thread in `vars`, so use it for small csv file only.
 * For big csv file, please use JMeter native CSV Data Set Config.
 *
 * Arguments:
 *   1. varName         String. The var where the loaded data save to.
 *   2. filePath        String. The csv file to be loaded.
 *
 * Options:
 *   -. separator       Char (','). The csv separator.
 *   -. quote           Char ('"'). The csv quote.
 *   -. returnFormat    Char ('l'). Return format: 'l' - list of lists, 'm' - list of maps.
 *   -. skipFirstLine   Boolean (false). Skip first line (only for list of lists).
 *
 */

import groovy.json.*
import org.apache.jorphan.util.JOrphanUtils

// default params
def varName         = args[0]
def filePath        = args[1]
def separator       = ','
def quote           = '"'
def returnFormat    = 'l'
def skipFirstLine   = false

// input params
args.each { arg ->
    def parts = arg.tokenize('=')
    if (parts.size() > 1) {
        switch (parts[0].toLowerCase()) {
            case 'separator':   separator = parts[1];   break
            case 'quote':       quote     = parts[1];   break
            case 'returnformat':
                switch (parts[1].toLowerCase()) {
                    case 'l': case 'list': case 'lists':
                        returnFormat = 'l'
                        break
                    case 'm': case 'map': case 'maps':
                        returnFormat = 'm'
                        break
                }
                break
            case 'skipfirstline':
                switch (parts[1].toLowerCase()) {
                    case '1': case 'true':
                        skipFirstLine = true
                        break
                    default:
                        skipFirstLine = false
                }
                break
        }
    }
}

if (!varName || !filePath) {
    SampleResult.setSuccessful(false)
    SampleResult.setResponseCode("500")
    SampleResult.setResponseMessage("Expected 2 args: varName and filePath.")
    SampleResult.setResponseData("")
}

// read data from file
def result = []
def maxColumn = 0

def file = new File(filePath)
file.withReader { reader ->
    while (line = reader.readLine()) {
        def lineData = []
        def cell = ''
        def lineTokenize = JOrphanUtils.split(line, separator, false).toList()
        lineTokenize.each { it ->
            def save = false
            if (cell == '') {
                cell += it
                save =
                        !it.startsWith(quote) ||
                                (it.startsWith(quote) && it.endsWith(quote) && !it.endsWith("\\" + quote) && it.length() > 1)
            } else {
                cell += separator + it
                save = it.endsWith(quote) && !it.endsWith("\\" + quote)
            }
            if (save) {
                if (cell.startsWith(quote)) {
                    cell = cell.substring(1)
                    if (cell.endsWith(quote) && !cell.endsWith("\\" + quote)) {
                        cell = cell.substring(0, cell.length() - 1)
                    }
                }
                lineData << cell
                cell = ''
            }
        }
        maxColumn = Math.max(maxColumn, lineData.size())
        result << lineData
    }
}

// fix number of columns
result.eachWithIndex { it, i ->
    for (j = it.size(); j < maxColumn; j++) {
        result[i][j] = ''
    }
}

// if return format is a list
if (returnFormat == 'l') {
    if (skipFirstLine) {
        result.removeAt(0)
    }
}

// if return format is a map
if (returnFormat == 'm') {
    def result2 = []
    def headers = result[0]
    result.removeAt(0)
    result.each { it ->
        def record = new LinkedHashMap()
        headers.eachWithIndex { header, i ->
            record[header] = it[i]
        }
        result2 << record
    }
    result = result2
}

// save to var
vars.putObject(varName, result)

// completed successful
SampleResult.setSuccessful(true)
SampleResult.setResponseCode("200")
SampleResult.setResponseMessage("")
SampleResult.setResponseData(JsonOutput.toJson(result))
