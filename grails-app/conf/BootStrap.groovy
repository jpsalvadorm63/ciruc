class BootStrap {

    def init = { servletContext ->
      def varName = ciruc2.Control.findByVarName('started')
      if(!varName) {
        varName = new ciruc2.Control()
        varName.varName = 'started'
        varName.currentValue = 0
        varName.save(flush:true)
      } else {
        varName.currentValue = 0
        varName.save(flush:true)
      }

      varName = ciruc2.Control.findByVarName('running')
      if(!varName) {
        varName = new ciruc2.Control()
        varName.varName = 'running'
        varName.currentValue = 0
        varName.save(flush:true)
      } else {
        varName.currentValue = 0
        varName.save(flush:true)
      }

      ciruc2.SourceFile.findAllByLoading(true).each { sf ->
        sf.loading = false
        sf.loaded = false
        sf.save(flush:true)
      }

    }

    def destroy = {
    }

}
