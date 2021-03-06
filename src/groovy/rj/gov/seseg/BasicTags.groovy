package rj.gov.seseg

import javax.servlet.http.HttpServletRequest

abstract class BasicTags {

    private static final String ATTR_TEMPLATE = "%s=\"%s\" "
    private static final String OPEN_TAG = "<%s "
    private static final String CLOSE_ATTR = ">"
    private static final String CLOSE_TAG = "</%s>"


    def criarTag(String tagName, String type, String id) {
        StringBuffer out = new StringBuffer()
        out.append(String.format(OPEN_TAG, tagName))
        out.append(criaAtributo("type", type))
        out.append(criaAtributo("id", id))
        return out
    }

    def criarTag(String tagName) {
        StringBuffer out = new StringBuffer()
        out.append(String.format(OPEN_TAG, tagName))
        return out
    }

    def criaAtributo( tipo, valor){
        String.format(ATTR_TEMPLATE, tipo, valor)
    }

    def criaAtributoFinal( tipo, valor){
        String.format(ATTR_TEMPLATE, tipo, valor) + CLOSE_ATTR
    }

    def fechaTag(String tagName) {
        String.format(CLOSE_TAG, tagName)
    }

    def retornaURLAction = { HttpServletRequest request, controller, action ->

        String scheme =         request.getScheme();         // http
        String serverName =     request.getServerName();     // hostname.com
        int serverPort =        request.getServerPort();     // 80
        String contextPath =    request.getContextPath();    // /mywebapp

        "${scheme}://${serverName}:${serverPort}${contextPath}/${controller}/${action}"
    }

    /*
    ForceId caso seja true a tag vai ficar com o id que o usuário passou, se não vai gerar e concatenar
     */
    def gerarID = { id, forcedId ->
        if(!forcedId){
            id =  id?id:""
            System.nanoTime() + "_" + id
        }
        else
            id
    }

    def criaAtributos(attrs){
        StringBuffer out = new StringBuffer()
        int ultimo = attrs.size()-1
        attrs.eachWithIndex {k, v, i ->
            if (i != ultimo)
                out.append(criaAtributo(k,v))
            else
                out.append(criaAtributoFinal(k,v))
        }
        return out
    }

    //envolverEmJquery , envolverEmSeseg , aSelecaoDoObjeto , valorSelecaoQuery , recebeAFuncao , funcaoScript, out
    def envolverEmJavascript = { Object[] args ->
        def out = args[args.length-1]
        String retorno

        def identificacaoInicioScript = "<script type=\"text/javascript\">"
        def identificacaoTerminoScript = "</script>"
        //passando todos os parametros
        if (args.length == 7){
            retorno = """${identificacaoInicioScript}${args[0].call( args[1].call(args[2].call(args[3]), args[4].call(args[5])) )}${identificacaoTerminoScript}"""
        }
        /*  envolverEmJavascript envolverEmSeseg, aSelecaoDoObjeto, ".teste", recebeAFuncao, "recebeFoco()"
            envolverEmJavascript envolverEmJquery, aSelecaoDoObjeto, ".teste", recebeAFuncao, "recebeFoco()" */
        else if (args.length == 6) {
            if (args[0].class.name.equals(envolverEmSeseg.class.name) ){
                retorno = """${identificacaoInicioScript}${ args[0].call(args[1].call(args[2]), args[3].call(args[4])) }${identificacaoTerminoScript}"""
            }
            else if( (args[0].class.name.equals(envolverEmJquery.class.name)) && (!args[1].class.name.equals(envolverEmSeseg.class.name)) ){
                def script = """${args[1].call(args[2]) }.${args[3].call(args[4])}"""
                retorno = """${identificacaoInicioScript}${ args[0].call(script) }${identificacaoTerminoScript}"""
            }
            else if( (args[0].class.name.equals(envolverEmJquery.class.name)) && (args[1].class.name.equals(envolverEmSeseg.class.name)) ){
                retorno = """${identificacaoInicioScript}${ args[0].call( args[1].call( args[2], args[3].call(args[4]) ) ) }${identificacaoTerminoScript}"""
            }
        }
        //envolverEmJavascript envolverEmSeseg, ".teste", recebeAFuncao, "recebeFoco()", out
        //ou envolverEmJavascript aSelecaoDoObjeto, "campo", recebeAFuncao, "focus()", out
        else if (args.length == 5){
            if (args[0].class.name.equals(envolverEmSeseg.class.name) ){
                retorno = """${identificacaoInicioScript}${args[0].call(args[1], args[2].call(args[3])) }${identificacaoTerminoScript}"""
            }
            else if (args[0].class.name.equals(aSelecaoDoObjeto.class.name) ){
                retorno = """${identificacaoInicioScript}${args[0].call(args[1])}.${args[2].call(args[3])}${identificacaoTerminoScript}"""
            }
        }
        out << retorno
    }


    def envolverEmJquery = { envolverEmQuery->

        def identificacaoInicioJquery = "jQuery(function(){"
        def identificacaoTerminoJquery = "});"

        """${identificacaoInicioJquery}${envolverEmQuery}${identificacaoTerminoJquery}""".toString()

    }

    def envolverEmSeseg = { def param1, String param2 ->
        def identificacaoInicio = "SESEG("
        def identificacaoTermino = ")"

        if(param1 instanceof GString){
            """${identificacaoInicio}${param1}${identificacaoTermino}.${param2}""".toString()
        }
        else if(param1 instanceof String){
            """${identificacaoInicio}'${param1}'${identificacaoTermino}.${param2}""".toString()
        }
    }

    def aSelecaoDoObjeto = { obj ->
        def identificacaoInicio = "jQuery('"
        def identificacaoTermino = "')"

        """${identificacaoInicio}${obj}${identificacaoTermino}"""
    }

    def recebeAFuncao = { String funcaoScript ->
        return funcaoScript + ";"
    }
}
