package ciruc2

class Control {

  String varName
  int currentValue

  static constraints = {
    varName maxSize: 24, nullable: false
  }

  static mapping = {
    table 'controlvalue'
  }

}
