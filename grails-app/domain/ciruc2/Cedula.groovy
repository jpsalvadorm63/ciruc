package ciruc2

class Cedula {

    String ciruc
    String codigoError
    String tipo
    String nombre
    // CI
    String condicionCedulado
    Date   fechaNacimiento
    String lugarNacimiento
    String estadoCivil
    String instruccion
    String genero
    String conyuge
    // RUC
    String direccionLarga
    String direccionCorta
    String telefonoTrabajo
    String telefonoDomicilio
    String actividadEconomica
    String nombreComercial
    String representanteLegal
    String representanteLegalId
    String condicionTributaria
    String estado

    static constraints = {
        ciruc              maxSize: 16, nullable: false
        codigoError        maxSize: 16, nullable: true
        tipo               maxSize: 8, inList: ['CI','RUC','?'], nullable: false
        nombre             maxSize: 128, nullable: true

        // CI
        condicionCedulado  maxSize: 128,  nullable: true
        fechaNacimiento    nullable: true
        lugarNacimiento    maxSize: 128, nullable: true
        estadoCivil        maxSize: 32,  nullable: true
        instruccion        maxSize: 32,  nullable: true
        genero             maxSize: 32,  nullable: true
        conyuge            maxSize: 128, nullable: true

        // RUC
        direccionLarga        maxSize: 248, nullable: true
        direccionCorta        maxSize: 248, nullable: true
        telefonoTrabajo       maxSize: 128,  nullable: true
        telefonoDomicilio     maxSize: 128,  nullable: true
        actividadEconomica    maxSize: 248, nullable: true
        nombreComercial       maxSize: 128, nullable: true
        representanteLegal    maxSize: 128, nullable: true
        representanteLegalId  maxSize: 24,  nullable: true
        condicionTributaria   maxSize: 8,   nullable: true
        estado                maxSize: 32,  nullable: true
    }

    static mapping = {
        table              'cedula'
        version            true
        cache              false

        id                 column:'id'
        ciruc              column:'ciruc',              index:'cirucidx'
        codigoError        column:'codigoerror'
        tipo               column:'tipo'
        nombre             column:'nombre'
        // CI
        condicionCedulado  column:'condicioncedulado'
        fechaNacimiento    column:'fechanacimiento'
        lugarNacimiento    column:'lugarnacimiento'
        estadoCivil        column:'estadocivil'
        instruccion        column:'instruccion'
        genero             column:'genero'
        conyuge            column:'comyuge'
        // RUC
        direccionLarga        column:'direccionlarga'
        direccionCorta        column:'direccioncorta'
        telefonoTrabajo       column:'telefonotrabajo'
        telefonoDomicilio     column:'telefonodomicilio'
        actividadEconomica    column:'actividadeconomica'
        nombreComercial       column:'nombreComercial'
        representanteLegal    column:'representantelegal'
        representanteLegalId  column:'representantelegalid'
        condicionTributaria   column:'condiciontributaria'
        estado                column:'estado'
    }

    static Cedula createCI(String csv) {
        def res = csv.split(';')
        if(res && res.length >= 13 && res[0] == '0') {
            def ci = new Cedula()
            ci.ciruc = res[1]
            ci.codigoError = res[0]
            ci.tipo = 'CI'
            ci.nombre = res[2]
            ci.condicionCedulado = res[3]
            ci.fechaNacimiento = Date.parse('dd/MM/yyyy',res[4])
            ci.lugarNacimiento = res[5]
            ci.estadoCivil = res[6]
            ci.instruccion = res[11]
            ci.genero = res[12]
            ci.conyuge = ((res[7]!=null)?res[7]:"")

            saveCIRUC(ci)
            return ci
        } else {
            return null
        }
    }

    static Cedula createRUC(String csv) {
        def res = csv.split(';')
        if(res && res.length >= 13 && res[0] == '0') {
            def ci = new Cedula()
            ci.ciruc = res[1]
            ci.codigoError = res[0]
            ci.tipo = 'RUC'
            ci.nombre = res[2]
            ci.direccionLarga = ((res[3]!=null)?(res[3]):"")
            ci.direccionCorta = ((res[4]!=null)?(res[4]):"")
            ci.telefonoTrabajo = ((res[5]!=null)?(res[5]):"")
            ci.telefonoDomicilio = ((res[6]!=null)?(res[6]):"")
            ci.actividadEconomica = ((res[7]!=null)?(res[7]):"")
            ci.nombreComercial = ((res[8]!=null)?(res[8]):"")
            ci.representanteLegal = ((res[9]!=null)?(res[9]):"")
            ci.representanteLegalId = ((res[10]!=null)?(res[10]):"")
            ci.condicionTributaria = ((res[11]!=null)?(res[11]):"")
            ci.estado = ((res[12]!=null)?(res[12]):"")

            saveCIRUC(ci)
            return ci
        } else {
            return null
        }
    }

    static def saveCIRUC(ci) {
        ci.save(flush:true)
        if(ci.hasErrors()) {
            ci.errors.each() {
                println "---> $it"
            }
        }
    }

}
