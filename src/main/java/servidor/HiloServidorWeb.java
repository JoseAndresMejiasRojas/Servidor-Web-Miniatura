package servidor;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import respuestas.HttpRespuesta;
import respuestas.RespuestaGET;
import respuestas.RespuestaHEAD;
import respuestas.RespuestaNoImplementado;
import respuestas.RespuestaPOST;
import respuestas.Solicitud;

public class HiloServidorWeb implements Runnable 
{
	private Socket socket;
	private HttpRespuesta respuesta;
	
	public HiloServidorWeb(Socket socket) 
	{
		this.socket = socket;
		respuesta = new RespuestaGET();
		respuesta.construirCadenaDeSolicitudes(new RespuestaHEAD())
					.construirCadenaDeSolicitudes(new RespuestaPOST())
					.construirCadenaDeSolicitudes(new RespuestaNoImplementado());
		
	}

	@Override
	public void run()
	{
		try 
		{
			procesarSolicitud(socket);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void procesarSolicitud(Socket socket) throws IOException
	{
		// Para leer la solicitud del cliente
		BufferedInputStream  entrada = new BufferedInputStream(socket.getInputStream());
		// Para responderle al cliente en bytes
        OutputStream salida =  socket.getOutputStream();
                
		Solicitud solicitud = procesarEntrada(entrada);
		respuesta.chequearSolicitud(solicitud, salida);

        salida.flush();
        salida.close();
        entrada.close();
        socket.close();
	}
	
	private Solicitud procesarEntrada(BufferedInputStream entrada) throws IOException
	{
        String linea = leerLineaDesdeBytes(entrada);
        String llave = "";
        String valor = "";
        Solicitud solicitud = new Solicitud(entrada);
        int index_espacio = 0;

		while(linea.length() > 0)
		{
			index_espacio = linea.indexOf(' ');
			
			if(linea.contains(":"))
			{
				// Para no incluir los :
				
				llave = linea.substring(0, index_espacio - 1);
				valor = linea.substring(index_espacio + 1);

			}
			else // Es el request
			{
				llave = "Url";
				
				// Incluir la expresion del medio, no la version http
				valor = linea.substring(index_espacio + 1);
				valor = valor.substring(0, valor.indexOf(' '));
				
				// Agrego el tipo de request a la solicitud
				solicitud.agregarEncabezado("TipoRequest", linea.substring(0, index_espacio));
			}
			
			solicitud.agregarEncabezado(llave, valor);
			linea = leerLineaDesdeBytes(entrada);
		}
		
		return solicitud;
	}
	
	private String leerLineaDesdeBytes(BufferedInputStream in) throws IOException
	{
	    StringBuilder constructor_string = new StringBuilder();
	    
	    char caracter;

	    while ((caracter = (char) in.read()) >= 0) 
	    {
	        if (caracter == '\n') 
	        	break;
	        
	        if (caracter == '\r') 
	        {
	            caracter = (char) in.read();
	            if ((caracter < 0) || (caracter == '\n'))
	            	break;
	            
	            constructor_string.append('\r');
	        }
	        constructor_string.append(caracter);
	    }
	    
	    return constructor_string.toString();
	}
}
