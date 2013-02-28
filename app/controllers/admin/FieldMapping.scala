package controllers.admin

import scala.collection.mutable.HashMap

object FieldMapping {
    val agricultureAndVeterinary = "Agriculture & Veterinary"
    val architecture = "Architecture"
    val artsAndHumanities = "Creative Arts & Writing"
    val biology = "Biology"
    val chemistry = "Chemistry"
    // val commerceAndManagement = 	"Commerce & Management"
    val computerScience = "Computer Science"
    val earthSciences = "Earth Sciences"
    val economicsAndBusiness = "Economics & Business"
    val education = "Education"
    val engineering = "Engineering"
    val environmentalSciences = "Environmental Sciences"
    val historyAndArchaeology = "History & Archaeology"
    val humanities = "Humanities"
    val languageAndCulture = "Language & Culture"
    val legalStudies = "Legal Studies"
    val materialScience = "Material Science"
    val mathematics = "Mathematics"
    val medecineAndHealth = "Medicine & Health"
    val multidisciplinary = "Multidisciplinary"
    val philosophyAndReligions = "Philosophy & Religions"
    val physics = "Physics"
    val psychology = "Psychology"
    val socialSciences = "Social Science"
    val technology = "Technology"
    val unknown = "Unknown"

    val fieldMapping = HashMap(
        // Microsoft AR
        "Agriculture Science" -> agricultureAndVeterinary,
        "Arts & Humanities" -> artsAndHumanities,
        "Biology" -> biology,
        "Chemistry" -> chemistry,
        "Computer Science" -> computerScience,
        "Economics & Business" -> economicsAndBusiness,
        "Engineering" -> engineering,
        "Environmental Sciences" -> environmentalSciences,
        "Geosciences" -> earthSciences,
        "Material Science" -> materialScience,
        "Mathematics" -> mathematics,
        "Medicine" -> medecineAndHealth,
        "Physics" -> physics,
        "Social Science" -> socialSciences,
        "Multidisciplinary" -> multidisciplinary,

        // Core
        "Mathematical Sciences" -> mathematics,
        "Physical Sciences" -> physics,
        "Chemical Sciences" -> chemistry,
        "Earth Sciences" -> earthSciences,
        "Environmental Sciences" -> environmentalSciences,
        "Biological Sciences" -> biology,
        "Agricultural and Veterinary Sciences" -> agricultureAndVeterinary,
        "Information and Computing Sciences" -> computerScience,
        "Engineering" -> engineering,
        "Technology" -> technology,
        "Medical and Health Sciences" -> medecineAndHealth,
        "Built Environment and Design" -> architecture,
        "Education" -> education,
        "Economics" -> economicsAndBusiness,
        "Commerce, Management, Tourism and Services" -> economicsAndBusiness,
        "Studies in Human Society" -> humanities,
        "Psychology and Cognitive Sciences" -> psychology,
        "Law and Legal Studies" -> legalStudies,
        "Studies in Creative Arts and Writing" -> artsAndHumanities,
        "Language, Communication and Culture" -> languageAndCulture,
        "History and Archaeology" -> historyAndArchaeology,
        "Philosophy and Religious Studies" -> philosophyAndReligions
    )
}