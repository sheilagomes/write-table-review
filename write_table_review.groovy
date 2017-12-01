/* :name=       Write Table Review:description=Exports the corrected project into HTML 
 *                table with corrected segments highlighted.
 * 
 *
 * #Purpose:    Export the review project into a HTML table
 * #Files:      Writes 'project_table.html' in the 'script_output' subfolder
 *              of current project's root. Each source file is exported to a separate
 *              table that has the name of the file as its heading, all source segments
 *              in the left column and corresponding target segments in the right, 
 *              highlighted when corrected. Project should have same language for both
 *              source and target
 * 
 * @author:     Kos Ivantsov, adapted by Sheila Gomes
 * @date:       2017-12-01
 */

def autoopen    = true

def tagbg       = '#E5E4E2'   //tag background in context
def tagfg       = 'green'     //tag foreground in context
def contxtfg    = 'darkgray'  //font foreground in context
def notebg      = 'yellow'    //note background
def curtagbg    = '#BCC6DD'   //tag background in segment's text
def curtagfg    = 'green'     //tag foreground in segment's text
def curtxtbg    = '#BCC6CC'   //font background in segment's text
def filenmbg    = 'lightgray' //cell background for filename
def thbg        = 'gray'      //table heading cells background
import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*
import org.omegat.util.StaticUtils
import org.omegat.util.StringUtil

def prop = project.projectProperties
if (!prop) {
    final def title = 'Export project to table'
    final def msg   = 'Please try again after you open a project.'
    showMessageDialog null, msg, title, INFORMATION_MESSAGE
    return
}
utils = (StringUtil.getMethods().toString().findAll("makeValidXML")) ? StringUtil : StaticUtils

def folder = prop.projectRoot+'script_output/'
projname = new File(prop.getProjectRoot()).getName()
table_file = new File(folder + projname + '.html')
// create folder if it doesn't exist
if (! (new File (folder)).exists()) {
    (new File(folder)).mkdir()
    }

def painttag = {x, tbg, tfg -> x.replaceAll(/(\&lt\;\/?\s?\w+\s?\/?\d+?\s?\/?\s?\/?\&gt\;)/, /\<sup\>\<font size=\"1\"  style=\"background-color:$tbg\; color:$tfg" \>$1\<\/font\>\<\/sup\>/)}
count = 0
table_file.write("""\
<html>\n<head>
<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">
<body>""", 'UTF-8')

//files = project.projectFiles.subList(editor.@displayedFileIndex, editor.@displayedFileIndex + 1); 
files = project.projectFiles
    for (i in 0 ..< files.size())
    {
        fi = files[i]
        table_file.append("""\
  <table border=\"1px\" style=\"margin-bottom:50px\" width=\"100%\">
  <tr align=\"center\"><th style=\"border:1px solid black\" colspan=\"3\" width=\"100%\" bgcolor=lightgray><strong>$fi.filePath</strong></th></tr>\n""", 'UTF-8')
        for (j in 0 ..< fi.entries.size())
        {
            def ignore = ''
            ste = fi.entries[j]
            source = ste.getSrcText()
            if (source ==~ /^(<\/?[a-z]+[0-9]* ?\/?>)+$/ ){
            ignore = 'yes'
            }
            seg_num = ste.entryNum()
            target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
            if (target == null)
            target = "zzznullzzz"
            if (target.size() == 0 )
            target = "<EMPTY>"
            source = utils.makeValidXML(source)
            source = painttag(source, curtagbg, tagfg)
            target = utils.makeValidXML(target).replaceAll(/zzznullzzz/, /&#8288;/)
            target = painttag(target, curtagbg, tagfg)
            if (target != source){
            table_file.append("""\
              <tr>
                <td style=\"border:1px solid black\" width=\"2%\">$seg_num</td>
                <td style=\"border:1px solid black\" width=\"49%\">$source</td>
                <td style=\"border:1px solid black\" width=\"49%\" bgcolor=yellow>$target</td>
              </tr>""", 'UTF-8')
            count++
            } else {
            if (ignore != 'yes'){
            table_file.append("""\
  <tr>
    <td style=\"border:1px solid black\" width=\"2%\">$seg_num</td>
    <td style=\"border:1px solid black\" width=\"49%\">$source</td>
    <td style=\"border:1px solid black\" width=\"49%\">$target</td>
  </tr>""", 'UTF-8')
            count++
            }
            }
        }
        table_file.append("  </table>\n", 'UTF-8')
    }
table_file.append("</body>\n</html>", 'UTF-8')
console.println "$count segments written to $table_file"
if (autoopen == true) {
    def command
    switch (osType) {
        case [OsType.WIN64, OsType.WIN32]:
            command = "cmd /c start \"\" \"$table_file\"" // for WinNT
            // command = "command /c start \"\" \"$table_file\"" // for Win9x or WinME
            break
        case [OsType.MAC64, OsType.MAC32]:
            command = ['open', table_file]
            break
        default:  // for Linux or others
            command = ['xdg-open', table_file]
            break
        }
    command.execute()
    }
return
