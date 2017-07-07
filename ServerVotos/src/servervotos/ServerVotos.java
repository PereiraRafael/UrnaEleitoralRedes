/**
 *
 * @author Junio
 */
package servervotos;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael, Gabriel e Alair
 */
public class ServerVotos {

    public static void main(String[] args) {

        boolean bEscutando = true;
        int porta = 40008;
        ServerSocket OUVIDO = null;

        // Inicializacao dos candidatos caso não tenha arquivo salvo
        File fTemp = new File("candidatos.dat");
        if (!fTemp.exists()) {
            try {
                fTemp.createNewFile();

                ObjectOutputStream serializador = new ObjectOutputStream(new FileOutputStream(fTemp));
                ArrayList<Candidato> candidatos = (ArrayList<Candidato>) new ArrayList();
                int brancos = 0, nulos = 0;

                candidatos.add(new Candidato(10, "Jo", "Corinthians", 0));
                candidatos.add(new Candidato(25, "Cueva", "Sao Paulo", 0));
                candidatos.add(new Candidato(57, "Dudu", "Palmeiras", 0));

                serializador.writeObject(candidatos);
                serializador.writeObject(brancos);
                serializador.writeObject(nulos);

                serializador.flush();
                serializador.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerVotos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        

        /* Disponibilizando a porta 40008 para conexões. */
        try {
            OUVIDO = new ServerSocket(porta);
            System.out.println("Escutando multiplos clientes na porta "+ porta + "...");
            
            /* Ativando controlador do servidor: encerra o servidor ao receber a palavra close. */
            ThreadControler t = new ThreadControler(OUVIDO);
            t.start();

            while (bEscutando) {
                Socket SERVIDOR_SOCKET = OUVIDO.accept();
                System.out.println("Conexao aceita");
                ServerThread threadServidora = new ServerThread(SERVIDOR_SOCKET); // Criação de Thread para manipular conexão
                
                threadServidora.start();
                System.out.println("Manipulador de conexao atribuido");
            }
            
            OUVIDO.close(); // Fechando a porta de conexão com servidor
            
        } catch (IOException e) {
            System.err.println("Nao pude escutar na porta: " + porta);
            System.exit(-1);
        }
    }
}
