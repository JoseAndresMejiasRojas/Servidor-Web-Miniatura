package respuestas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;

import servidor.ProcesadorXML;
import servidor.ServidorWeb;

public abstract class HttpRespuesta 
{
	private ProcesadorXML procesadorXML;
	
	protected HttpRespuesta siguiente;

	
	protected HttpRespuesta()
	{
		procesadorXML = new ProcesadorXML();
	}
	
	// Chain of responsability
	public HttpRespuesta construirCadenaDeSolicitudes(HttpRespuesta siguiente)
	{
		this.siguiente = siguiente;
		return siguiente;
	}
		
	// Incluye el fin de los encabezados
	protected void enviarEncabezadosComunes(OutputStream salida, File archivoSolicitado) throws IOException
	{		
		salida.write(convertirBytes("Date: " + ServidorWeb.obtenerFechaServidorGMT() + "\r\n")); // FALTA FORMATO
		salida.write(convertirBytes("Server: " + ServidorWeb.NOMBRE_SERVIDOR + "\r\n"));
		
		try 
		{
			if(archivoSolicitado != null)
			{
				salida.write(convertirBytes("Content-Length: " + archivoSolicitado.length() +"\r\n"));
				
				// Extraer el dominio y obtener el tipo de acuerdo a web.xml
				salida.write(convertirBytes("Content-Type: " + procesadorXML.obtenerTipo(obtenerExtension(archivoSolicitado)) + "\r\n"));
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		salida.write(convertirBytes("\r\n")); // Listo los headers, lo siguiente es el mensaje como tal
	}
	
	protected byte[] convertirBytes(String string)
	{
		return string.getBytes();
	}
	
	protected String obtenerExtension(File archivo)
	{
		String fileName = archivo.getName();
		
		// Si hay . y si el nombre no termina con .
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        {
            return fileName.substring(fileName.lastIndexOf(".")+1);
        }
        else 
        {
        	return "";
        }
	}
	
	protected void responderArchivo(OutputStream salida, File archivo) throws IOException
	{
		InputStream entrada = new FileInputStream(archivo);
		
		ByteStreams.copy(entrada, salida);
	}
	
	protected String extraerMensaje(char[] linea)
	{
		int inicio = -1;
		int fin = -1;
		for(int contador = 0; contador < linea.length; ++contador)
		{
			if(linea[contador] == ' ')
			{
				if(inicio == -1)
				{
					inicio = contador + 1;
				}
				else
				{
					fin = contador;
					break;
				}
			}
		}

		return new String(linea).substring(inicio, fin);
	}
	
	// Procesa la url dada y las variables que si existen. Devuelve un Url con el link fisico, 
	protected Url procesarUrl(String mensajeSolicitud, OutputStream salida) throws IOException
	{
		String datos = "";
		String url = "";
		// mensajeSolicitud tiene / de primero
		String path = ServidorWeb.PATH_RAIZ;
		int indexDatos = mensajeSolicitud.indexOf('?');
		
		// Existen datos con ?
		if( indexDatos >= 0)
		{
			datos = mensajeSolicitud.substring(indexDatos + 1);
			url = mensajeSolicitud.substring(0, indexDatos); // No incluir lo que hay despues del ?
			path += url;
		}
		else
		{
			url = mensajeSolicitud;
			path += url;
		}

		// Si la solicitud se hace sin especificar archivo, se supone un index.html
		if(url.length() > 0 && url.charAt(url.length() - 1) == '/' )
		{			
			path = path.concat("index.html");
		}
		
		return new Url.Builder(url, path).agregarDatos(datos).build();
	}
	
	public void chequearSolicitud(Solicitud solicitud, OutputStream salida)
	{
		if(procesarSolicitud(solicitud, salida) == false)
		{
			siguiente.chequearSolicitud(solicitud, salida);
		}
	}
	
	public abstract boolean procesarSolicitud(Solicitud solicitud, OutputStream salida);
		
}
