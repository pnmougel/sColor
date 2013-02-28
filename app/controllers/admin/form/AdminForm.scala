package controllers.admin.form

case class AdminForm(fields: FormField*) {
    def toHtml(): String = {
        fields.map(_.toHtml).mkString("\n")
    }

    lazy val size = fields.size
}

abstract class FormField(name: String, label: String) {
    def toHtml(): String

    def mergeArgs(args: (String, String)*): String = args.map(p => p._1 + "=\"" + p._2 + "\"").mkString(" ")
}

case class TextField(name: String, label: String, args: (String, String)*) extends FormField(name, label) {
    def toHtml() = {
        "<input name=\"" + name + "\" id=\"" + name + "\"" + mergeArgs(args: _*) + "/>"
    }
}

case class SelectField(name: String, label: String, options: Seq[(String, Any)], default: Option[String] = None,
                       args: Seq[(String, String)] = List()) extends FormField(name, label) {
    def toHtml() = {
        val optionsStr: Seq[(String, String)] = options.map {
            option => (option._1, option._2.toString)
        }
        "<select name=\"" + name + "\" id=\"" + name + "\"" + mergeArgs(args: _*) + ">" +
          optionsStr.map {
              opt =>
                  val isSelected = opt._2 == default.getOrElse("")
                  "<option value\"" + opt._2 + "\"" + (if (isSelected) " selected=\"selected\"" else "") + ">" + opt._1 + "</option>"
          }.mkString("\n") + "</select>"
    }
}

case class RadioField(name: String, label: String, options: Seq[(String, String)]) extends FormField(name, label) {
    def toHtml() = {
        ""
    }
}

object Test {
    def main(args: Array[String]): Unit = {
        val x = SelectField("a", "b", List("bla" -> ""))
    }
}