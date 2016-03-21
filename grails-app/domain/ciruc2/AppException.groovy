package ciruc2

class AppException {

  String appMessage
  String javaException
  String className
  String dateTime
  String data1
  String data2
  String data3
  String data4

  static constraints = {
    appMessage    maxSize: 254, nullable: true
    javaException maxSize: 254, nullable: true
    className     maxSize: 128, nullable: true
    dateTime      maxSize: 24, nullable: true
    data1         maxSize: 254, nullable: true
    data2         maxSize: 254, nullable: true
    data3         maxSize: 254, nullable: true
    data4         maxSize: 254, nullable: true
  }

  static mapping = {
    table    'appexception'
    version  true
    cache    false

    id             column:'id'
    appMessage     column:'appmessage'
    javaException  column:'javaexception'
    className      column:'classname'
    dateTime       column:'datetime'
    data1          column:'data1'
    data2          column:'data2'
    data3          column:'data3'
    data4          column:'data4'
  }

  static AppException addException(String appMsg, String javaMessage, String className) {
    AppException ae = new AppException()
    ae.appMessage = appMsg
    ae.javaException = javaMessage
    ae.className = className
    ae.dateTime = new Date().format('YYYY-MM-dd HH:mm:ss')
    ae.data1 = null
    ae.data2 = null
    ae.data3 = null
    ae.data4 = null
    ae.save(flush: true)

    return ae
  }


}
