package ciruc2

class SourceFile {

  String  fileName
  String  fileDate
  boolean loading
  boolean loaded
  Date loadingDate
  int tLines
  int lines
  int okLines

  static constraints = {
    fileName maxSize: 254, nullable: false
    fileDate maxSize: 32, nullable: false
    loading nullable: false
    loaded nullable: false
    loadingDate maxSize: 24, nullable: true
    tLines nullable: false, default: 0
    lines nullable: false, default: 0
    okLines nullable: false, default: 0
  }

  static mapping = {
    table    'sourcef'
    version  true
    cache    false

    id          column:'id'
    fileName    column:'filename'
    fileDate    column:'filedate'
    loading     column:'loading'
    loaded      column:'loaded'
    loadingDate column:'loadingdate'
    tLines      column:'tlines'
    lines       column:'lines'
    okLines     column:'oklines'
  }

  static int toList(File file) {
    if(file.exists() && file.isFile()) {
      String fn = file.getCanonicalPath()
      String df = new Date(file.lastModified()).format('YYYY-MM-dd HH:mm:ss')
      SourceFile sf = findByFileName(fn)
      boolean saveFile = ( ( sf == null ) || (sf.fileDate < df) )
      try {
        if( (sf != null) && (sf.fileDate < df) )
          sf.delete()
        if(saveFile) {
          sf = new SourceFile()
          sf.fileName = fn
          sf.fileDate = df
          sf.loading = false
          sf.loaded = false
          sf.loadingDate = null
          sf.save(flush:true)
          return 1
        } else
          return 0
      } catch(Exception e) {
          AppException.addException("toList(File), cannot save classs o cannot delete object for file $fn ($df)",e.message,'SourceFile')
          return 0
      }
    } else
        return 0
  }

}
