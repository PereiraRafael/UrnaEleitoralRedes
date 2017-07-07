/**
 *
 * @author Rafael, Gabriel e Alair
 */
package servervotos;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Classe com suporte a Threads */
public class ServerThread extends Thread {

    private ArrayList<Candidato> candidatos = new ArrayList<Candidato>();
    private Socket SERVIDOR_SOCKET = null;
    File fTemp;
    BufferedReader RECEBE;
    PrintWriter ENVIA;
    ObjectOutputStream ESCREVE;
    ObjectInputStream LE;

    public ServerThread(Socket umCLIENTE) {
        this.SERVIDOR_SOCKET = umCLIENTE;
        fTemp = new File("candidatos.dat");
        try {
            //Preparando manipuladore de Stream de dados
            RECEBE = new BufferedReader(new InputStreamReader(SERVIDOR_SOCKET.getInputStream()));
            ENVIA = new PrintWriter(new OutputStreamWriter(SERVIDOR_SOCKET.getOutputStream()));
        
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Manipulador de conexão - Parte a executar da Thread
    public void run() {
        try {
            String opt;
            int nopt;

            // Recebendo opção do cliente
            opt = RECEBE.readLine();
            nopt = Integer.parseInt(opt);
            switch (nopt) {
                case 999:
                    System.out.println("Opcao 999 escolhida");
                    this.enviaCandidatos();
                    break;
                case 888:
                    System.out.println("Opcao 888 escolhida");
                    this.recebeVotos();
                    break;
                default:
                    SERVIDOR_SOCKET.close();
                    System.out.println("Conexao encerrada");
            }
            
            ENVIA.close();
            RECEBE.close();
            SERVIDOR_SOCKET.close();
            System.out.println("Conexao encerrada");

        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int enviaCandidatos() {
        try {
            int brancos, nulos, i;
            //Preparando manipulador de leitura de arquivo
            LE = new ObjectInputStream(new FileInputStream(fTemp));
            // Colocando os dados do arquivo na memória
            candidatos = (ArrayList<Candidato>) LE.readObject();
            brancos = (int) LE.readObject();
            nulos = (int) LE.readObject();
            
            // Enviando dados de candidatos para o cliente
            ENVIA.println(candidatos.size());
            for (i = 0; i < candidatos.size(); i++) {
                ENVIA.println(candidatos.get(i).getCodigoVotacao());
                ENVIA.println(candidatos.get(i).getNomeCandidato());
                ENVIA.println(candidatos.get(i).getPartido());
                ENVIA.println(candidatos.get(i).getNumVotos());
            }
            ENVIA.println(brancos);
            ENVIA.println(nulos);
            
            LE.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /* Função de recebimento e atualização de votos, precisa ser sincronizada
    para não haver erro por concorrencia de Threads */
    synchronized public int recebeVotos() {
        try {
            int brancos, nulos, brancosRecebidos, nulosRecebidos, i, j;
            ArrayList<Candidato> cRecebidos = new ArrayList<Candidato>();
            //Preparando manipulador de leitura de arquivo
            LE = new ObjectInputStream(new FileInputStream(fTemp));
            
            // Colocando os dados do arquivo na memória
            candidatos = (ArrayList<Candidato>) LE.readObject();
            brancos = (int) LE.readObject();
            nulos = (int) LE.readObject();
            
            // Recebendo código e número de votos de candidato do cliente e armazenando em memória
            for (i = 0; i < candidatos.size(); i++) {
                cRecebidos.add(new Candidato(Integer.parseInt(RECEBE.readLine()),"","",Integer.parseInt(RECEBE.readLine())));
            }
            brancosRecebidos = Integer.parseInt(RECEBE.readLine());
            nulosRecebidos = Integer.parseInt(RECEBE.readLine());
            
            // Atualizando os votos dos candidatos
            System.out.println("Atualizando votos...");
            for (i=0; i<candidatos.size(); i++){
		for (j=0; j<candidatos.size(); j++){
			if(cRecebidos.get(j).getCodigoVotacao() == candidatos.get(i).getCodigoVotacao()){
				candidatos.get(i).setNumVotos(candidatos.get(i).getNumVotos()+cRecebidos.get(j).getNumVotos());
			}
		}
            }
            brancos += brancosRecebidos;
            nulos += nulosRecebidos;
            
            // Apaga o arquivo antigo e escreve um novo com os votos atualizados
            fTemp.createNewFile();
            ESCREVE = new ObjectOutputStream(new FileOutputStream(fTemp));
            
            ESCREVE.writeObject(candidatos);
            ESCREVE.writeObject(brancos);
            ESCREVE.writeObject(nulos);
            
            LE.close();
            ESCREVE.flush();
            ESCREVE.close();
            System.out.println("Votos atualizados");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
}
