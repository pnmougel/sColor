package models

import java.util.Date
import anorm._
import models.renorm.Table
import java.util.Calendar
import java.text.SimpleDateFormat

case class Update(var conferenceId: Long, var userId: Long, var createdAt: Date) {
    var id: Long = 0

    val dateFormated = {
        val suffixes = Array("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th", "th", "st")
        val calendar = Calendar.getInstance()
        calendar.setTime(createdAt)
        new SimpleDateFormat("dd'" + suffixes(calendar.get(Calendar.MONTH)) + "' 'of' MMM YYYY 'at' H':'mm").format(createdAt)
    }

    lazy val creationDateUpdate = UpdateCreationDate.findOption('update_id -> id)
    lazy val ctypeUpdate = UpdateCtype.findOption('update_id -> id)
    lazy val descriptionUpdate = UpdateDescription.findOption('update_id -> id)
    lazy val nameUpdate = UpdateName.findOption('update_id -> id)
    lazy val publisherUpdate = UpdatePublisher.findOption('update_id -> id)
    lazy val regionUpdate = UpdateRegion.findOption('update_id -> id)
    lazy val shortnameUpdate = UpdateShortname.findOption('update_id -> id)
    lazy val addSubfieldUpdates = UpdateAddSubfield.find('update_id -> id)
    lazy val removeSubfieldUpdates = UpdateRemoveSubfield.find('update_id -> id)
}

object Update extends Table[Update] {
    val defaultHistorySize = 10

    def create(conferenceId: Long, userId: Long): Long =
        super.createEntry('conference_id -> conferenceId, 'user_id -> userId, 'created_at -> new Date())
}

case class UpdateCreationDate(updateId: Long, before: Long, after: Long)

object UpdateCreationDate extends Table[UpdateCreationDate](Option("update_creation_date")) {
    override val idColumn = ""

    def create(updateId: Long, before: Int, after: Int): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateCtype(updateId: Long, before: Long, after: Long) {
    lazy val ctypeBefore = CType.byId(before).get
    lazy val ctypeAfter = CType.byId(after).get
}

object UpdateCtype extends Table[UpdateCtype](Option("update_ctype")) {
    override val idColumn = ""

    def create(updateId: Long, before: Long, after: Long): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateName(updateId: Long, before: String, after: String)

object UpdateName extends Table[UpdateName](Option("update_name")) {
    override val idColumn = ""

    def create(updateId: Long, before: String, after: String): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateDescription(updateId: Long, before: Option[String], after: Option[String])

object UpdateDescription extends Table[UpdateDescription](Option("update_description")) {
    override val idColumn = ""

    def create(updateId: Long, before: Option[String], after: Option[String]): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateShortname(updateId: Long, before: String, after: String)

object UpdateShortname extends Table[UpdateShortname](Option("update_shortname")) {
    override val idColumn = ""

    def create(updateId: Long, before: String, after: String): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdatePublisher(updateId: Long, before: Option[Long], after: Option[Long]) {
    lazy val publisherBefore = if (before.isDefined) Publisher.byId(before.get) else None
    lazy val publisherAfter = if (after.isDefined) Publisher.byId(after.get) else None
}

object UpdatePublisher extends Table[UpdatePublisher](Option("update_publisher")) {
    override val idColumn = ""

    def create(updateId: Long, before: Option[Long], after: Option[Long]): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateRegion(updateId: Long, before: Long, after: Long) {
    lazy val regionBefore = Region.byId(before).get
    lazy val regionAfter = Region.byId(after).get
}

object UpdateRegion extends Table[UpdateRegion](Option("update_region")) {
    override val idColumn = ""

    def create(updateId: Long, before: Long, after: Long): Long =
        super.createEntry('update_id -> updateId, 'before -> before, 'after -> after)
}

case class UpdateAddSubfield(updateId: Long, subfieldId: Long) {
    lazy val subfield = SubField.byId(subfieldId).get
}

object UpdateAddSubfield extends Table[UpdateAddSubfield](Option("update_subfield_added")) {
    override val idColumn = ""

    def create(updateId: Long, subfieldId: Long): Long =
        super.createEntry('update_id -> updateId, 'subfield_id -> subfieldId)
}

case class UpdateRemoveSubfield(updateId: Long, subfieldId: Long) {
    lazy val subfield = SubField.byId(subfieldId).get
}

object UpdateRemoveSubfield extends Table[UpdateRemoveSubfield](Option("update_subfield_removed")) {
    override val idColumn = ""

    def create(updateId: Long, subfieldId: Long): Long =
        super.createEntry('update_id -> updateId, 'subfield_id -> subfieldId)
}