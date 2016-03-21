package ciruc

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import groovyx.gpars.GParsPool

class ValidarController {

  def index() {
  }

  def security = ['20sTerras13':'sigtierras',
                  'inYPsaciRuc':'inipsa',
                  'CIcbpRUCa':'cbp',
                  'nipRucCi':'nipsa',
                  'blom':'20bloM14']

  def iniciaSesion() {
    def palabraClave = params.id
    println palabraClave

    if(palabraClave && security[palabraClave] != null) {
      session.httpClient = new DefaultHttpClient()
      session.postRequest = new HttpPost("http://192.168.1.5/Enlaces/Service.asmx")
      render  contentType: 'text/plain', text:"CIRUC: SESSION INICIADA", encoding: 'LATIN1'
    } else {
      render  contentType: 'text/plain', text:"CIRUC: PALABRA CLAVE NO VÁLIDA", encoding: 'LATIN1'
    }
  }

  def processFile() {
    def csvFile = new File('/var/sigtierras/' + params.id + '.csv')
    if(csvFile.exists() && csvFile.isFile()) {
      def csvResultados = new File('/var/sigtierras/' + params.id + '-ci.csv')
      if(csvResultados.exists())
        csvResultados.delete()

      def httpClient = new DefaultHttpClient()
      def postRequest = new HttpPost("http://192.168.1.5/Enlaces/Service.asmx")
      int i = 0

      def cedulas = []
      csvFile.eachLine { csvLine ->
        def items = csvLine.split(',')
        def ci = ((items.length >= 2)?items[1]:'-----')[1..-2]
        if(ci.length() == 10 && cedulas.indexOf(ci) < 0)
          cedulas << ci
      }
      println "\n\nCédulas a ser procesadas: ${cedulas.size()}\n"
      csvResultados.withWriter { out ->
        //GParsPool.withPool {
        cedulas.each { ci ->

          String strRequest = ""
          strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
          strRequest += '<soap:Header/><soap:Body><agr:WBConsultaCed>'
          strRequest += '<agr:cadena>' + ci + '</agr:cadena>'
          strRequest += '</agr:WBConsultaCed></soap:Body></soap:Envelope>'
          StringEntity input = new StringEntity(strRequest)
          input.setContentType("application/soap+xml")
          postRequest.setEntity(input)
          HttpResponse response = httpClient.execute(postRequest)
          def sc =response.getStatusLine().getStatusCode()
          def sl = ""
          def res0 = ''
          if(response.getStatusLine().getStatusCode() != 200)
            sl = "[ COMUNICACIONES UE-MAGAP : NO OK | ${response.getStatusLine().getStatusCode()} ]"
          else {
            sl = "[ COMUNICACIONES UE-MAGAP : OK | ${response.getStatusLine().getStatusCode()} ]"
            res0 = response.getEntity().getContent().text
            def j = res0.indexOf(';')
            if( j >= 0 )
              res0 = res0.substring(0,j+1) + ci + res0.substring(j)
          }
          def src =  new XmlSlurper().parseText(res0)
          res0 = ""+src.text()+""
          def resultado = res0.split(';')
          if(resultado && resultado.length >= 15 && resultado[0] == '0') {
            i++
            def newCsvLine = "$ci,${resultado[3]},${resultado[6]},${resultado[11]},${resultado[12]}\n"
            out.println "${i},${newCsvLine}\n"
            if(i % 100==0)
               println i
          }
        }
        //}
        out.close()
      }

      try {
        httpClient.getConnectionManager().shutdown()
      } catch (Exception e) {
        println "Error al cerrar la conexión"
      }
      httpClient = null
      postRequest = null

      render contentType: 'text/plain', text:"CIRUC: ARCHIVO ${csvFile} PROCESADO", encoding: 'LATIN1'
    } else {
      render contentType: 'text/plain', text:"CIRUC: ARCHIVO ${csvFile} NO PUEDE SER PROCESADO", encoding: 'LATIN1'
    }
  }

  def doc() {
    def resultado = "ERROR: ARGUMENTO VACIO"

    if(params.id != null) {
      String docId = "" + params.id.trim() + ""
      if(docId.length() == 10) {
        // consulta cédula
        resultado = 'CI:' + consultarCed(docId)
      } else
      if(docId.length() == 13) {
        // consulta RUC
        resultado = 'RUC:' + consultarRUC(docId)
      } else
        resultado = "ERROR: ARGUMENTO NO ES CEDULA NI DE RUC"
    }
    render contentType: 'text/plain', text:resultado, encoding: 'LATIN1'
  }

  def docUTF8() {
    def resultado = "ERROR: ARGUMENTO VACIO"

    if(params.id != null) {
      String docId = "" + params.id.trim() + ""
      if(docId.length() == 10) {
        // consulta cédula
        resultado = 'CI:' + consultarCed(docId)
      } else
      if(docId.length() == 13) {
        // consulta RUC
        resultado = 'RUC:' + consultarRUC(docId)
      } else
        resultado = "ERROR: ARGUMENTO NO ES CEDULA DE IDENTIDAD NI RUC"
    }
    println resultado
    render text:resultado, contentType:"text/plain", encoding:"UTF-8"
  }

  def docASCII() {
    def resultado = "ERROR: ARGUMENTO VACIO"

    if(params.id != null) {
      String docId = "" + params.id.trim() + ""
      if(docId.length() == 10) {
        // consulta cédula
        resultado = 'CI:' + consultarCed(docId)
      } else
      if(docId.length() == 13) {
        // consulta RUC
        resultado = 'RUC:' + consultarRUC(docId)
      } else
        resultado = "ERROR: ARGUMENTO NO ES CEDULA DE IDENTIDAD NI RUC"
    }
    println "1-> ${resultado}"
    resultado = resultado.replace('Ñ','N~').replace('ñ','n~').
        replace('á','a~').replace('é','e~').replace('í','i~').replace('ó','o~').replace('ú','u~').
        replace('Á','A~').replace('É','E~').replace('Í','I~').replace('Ó','O~').replace('Ú','U~')
    println "2-> ${resultado}"
    render text:resultado, contentType:"text/plain"
  }

  String consultarCed2(cedula) {
    String strRequest = ""
    strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
    strRequest += '<soap:Header/><soap:Body><agr:WBConsultaCed>'
    strRequest += '<agr:cadena>' + cedula + '</agr:cadena>'
    strRequest += '</agr:WBConsultaCed></soap:Body></soap:Envelope>'

    return consultar(strRequest,cedula)
  }

  String consultarCed(String id) {
    String strRequest = ""
    strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
    strRequest += '<soap:Header/><soap:Body><agr:WBConsultaCed>'
    strRequest += '<agr:cadena>' + id + '</agr:cadena>'
    strRequest += '</agr:WBConsultaCed></soap:Body></soap:Envelope>'

    return consultar(strRequest,id)
  }

  String consultarRUC(String id) {
    String strRequest = ""
    strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
    strRequest += '<soap:Header/><soap:Body>'
    strRequest += '<agr:WBConsultaRUC>'
    strRequest += '<agr:cadenaSRI>' + id + '</agr:cadenaSRI>'
    strRequest += '</agr:WBConsultaRUC></soap:Body></soap:Envelope>'

    return consultar(strRequest,id)
  }

  String consultar(String strRequest, String id) {
    if(session.postRequest == null || session.httpClient == null)
      return "CIRUC: ERROR LA SESION DE VALIDACIÓN NO HA SIDO INICIADA"
    else {
      StringEntity input = new StringEntity(strRequest)
      input.setContentType("application/soap+xml")
      session.postRequest.setEntity(input)
      HttpResponse response = session.httpClient.execute(session.postRequest)
      def sc =response.getStatusLine().getStatusCode()
      def sl = ""
      def resultado = ''
      if(response.getStatusLine().getStatusCode() != 200)
        sl = "[ COMUNICACIONES UE-MAGAP : NO OK | ${response.getStatusLine().getStatusCode()} ]"
      else {
        sl = "[ COMUNICACIONES UE-MAGAP : OK | ${response.getStatusLine().getStatusCode()} ]"
        resultado = response.getEntity().getContent().text
        def i = resultado.indexOf(';')
        if( i >= 0 )
          resultado = resultado.substring(0,i+1) + params.id + resultado.substring(i)
      }
      def src =  new XmlSlurper().parseText(resultado)
      resultado = ""+src.text()+""

      sleep(100)
      return resultado
    }
  }

  def finSesion() {
    try {
      httpClient.getConnectionManager().shutdown()
    } catch (Exception e) {

    }
    session.httpClient = null
    session.postRequest = null
    render "CIRUC: SESION FINALIZADA"
  }

}
