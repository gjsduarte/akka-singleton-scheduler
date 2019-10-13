package scheduling.model

object actions {

  final case class Schedule(taskName: String)
  final case class Execute(taskName: String)

}