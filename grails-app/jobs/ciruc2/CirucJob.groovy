package ciruc2

import org.apache.http.HttpResponse
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient

class CirucJob {

    static int i = 0

    static triggers = {
        simple repeatInterval: 60001
        // simple repeatInterval: 18001
    }

    static def httpClient
    static def postRequest

    def execute() {
        def cn

        // avoid initial error
        boolean started = (ciruc2.Control?.findByVarName('started').currentValue == 1)

        if(!started) {
            println 'NOT STARTED . . .'
            System.sleep(18000)
            cn = ciruc2.Control.findByVarName('started')
            cn.currentValue = 1
            cn.save(flush:true)
        }

        // update list of files to be processed
        File sourceDir = new File("/var/sigtierras/cedulas")
        sourceDir.eachFile() { f ->
            SourceFile.toList(f)
        }

        boolean enEjecucion = (Control.findByVarName('running').currentValue == 1)
        if(!enEjecucion) {
            cn = Control.findByVarName('running')
            cn.currentValue  = 1
            cn.save(flush:true)
            enEjecucion = true
            println "NO EN EJECUCION . . ."
            // define http connection ond open it
            boolean httpUp = httpInit()

            if(httpUp) {
                try {
                    SourceFile.findAllByLoaded(false).each() { sf ->
                        AppException.addException('start processing ${sf.fileName}','NO ERR','CirucJob')
                        def tLines = 0
                        def lines = 0
                        def okLines = 0

                        sf.loading = true
                        sf.tLines = tLines
                        sf.lines = lines
                        sf.okLines = okLines
                        sf.save(flush:true)
                        new File(sf.fileName).eachLine { line ->
                            tLines++
                            line = line.trim()
                            if(line.length() == 10 && !Cedula.findByCiruc(line)) {
                                if(line != '') {
                                    def res0 = consultarCed(line)
                                    Cedula.createCI(res0)
                                }
                            } else
                            if(line.length() == 13 && !Cedula.findByCiruc(line)) {
                                if(line != '') {
                                    def res0 = consultarRuc(line)
                                    Cedula.createRUC(res0)
                                }
                            }
                        }
                        sf.loading = false
                        sf.loaded = true
                        sf.tLines = tLines
                        sf.lines = lines
                        sf.okLines = okLines
                        sf.save(flush:true)
                        AppException.addException('finish processing ${sf.fileName}','NO ERR','CirucJob')
                    }

                    cn = Control.findByVarName('running')
                    cn.currentValue  = 0
                    cn.save(flush:true)
                    httpFinish()
                } catch (Exception e) {
                    cn = Control.findByVarName('running')
                    cn.currentValue  = 1
                    cn.save(flush:true)
                    println "ERROR - - - > ${e.getMessage()}"
                    AppException.addException('Error processing',e.getMessage(),'CirucJob')
                    httpFinish()
                }
            }
        }
    }

    boolean httpInit() {
        httpClient = null
        postRequest = null
        try {
            httpClient = new DefaultHttpClient()
            postRequest = new HttpPost("http://192.168.1.5/Enlaces/Service.asmx")
            return true
        } catch(Exception e) {
            AppException.addException('Error trying to open HTTP session',e.getMessage(),'CirucJob')
            httpClient = null
            postRequest = null
            return false
        }
    }

    String consultarCed(cedula) {
        String strRequest = ""
        strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
        strRequest += '<soap:Header/><soap:Body><agr:WBConsultaCed>'
        strRequest += '<agr:cadena>' + cedula + '</agr:cadena>'
        strRequest += '</agr:WBConsultaCed></soap:Body></soap:Envelope>'

        return ejecutar(strRequest, cedula)
    }

    String consultarRuc(ruc) {
        String strRequest = ""
        strRequest += '<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:agr="http://www.agricultura.gob.ec/">'
        strRequest += '<soap:Header/><soap:Body>'
        strRequest += '<agr:WBConsultaRUC>'
        strRequest += '<agr:cadenaSRI>' + ruc + '</agr:cadenaSRI>'
        strRequest += '</agr:WBConsultaRUC></soap:Body></soap:Envelope>'

        return ejecutar(strRequest, ruc)
    }

    String ejecutar(strRequest, id) {
        if(postRequest == null || httpClient == null) {
            return "CIRUC: ERROR LA SESION DE VALIDACIÓN NO HA SIDO INICIADA"
        } else {
            StringEntity input = new StringEntity(strRequest)
            input.setContentType("application/soap+xml")
            postRequest.setEntity(input)
            HttpResponse response = httpClient.execute(postRequest)
            def sc =response.getStatusLine().getStatusCode()
            def resultado = ''
            if(sc == 200) {
                resultado = response.getEntity().getContent().text
                def i = resultado.indexOf(';')
                if( i >= 0 ) {
                    resultado = resultado.substring(0,i+1) + id + resultado.substring(i)
                }
            }
            def src =  new XmlSlurper().parseText(resultado)
            resultado = "" + src.text() + ""
            sleep(24)
            return resultado
        }
    }

    void httpFinish() {
        if(httpClient != null) {
            try {
                httpClient.getConnectionManager().shutdown()
            } catch (Exception e) {
                AppException('Cant close HTP Connection',e.getMessage(),'CirucJob')
            }
        }
        httpClient = null
        postRequest = null
    }

}
