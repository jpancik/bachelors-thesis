package forms

import play.api.data.Form
import play.api.data.Forms._

object ParseLinesForm {

	val form: Form[String] = Form(
		single(
			"text" -> text
		)
	)
}
