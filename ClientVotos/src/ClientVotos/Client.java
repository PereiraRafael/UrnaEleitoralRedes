/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientVotos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author Rafael, Gabriel e Alair
 */

public class Client {
    Socket CLIENTE_SOCKET = null;
    PrintWriter ENVIA = null;
    BufferedReader RECEBE = null;
    ArrayList<Candidato> Candidatos = new ArrayList<Candidato>();
    int brancos, nulos, brancosTemp, nulosTemp;
    //String ip = "cosmos.lasdpc.icmc.usp.br";
    String ip = "localhost";
    int porta = 40008;
    
    /*Função que abre conexão com servidor para receber os dados dos candidatos*/
    public boolean recebeCandidatos(JTextArea ja) {
        String s ="", nome, partido;
        int qtd, cod, numVotos, flagErro = 0;
        
        try {
            //Abrindo conexão com servidor
            CLIENTE_SOCKET = new Socket(ip, porta);
            //Preparando manipuladore de Stream de dados
            ENVIA = new PrintWriter(CLIENTE_SOCKET.getOutputStream(), true);
            RECEBE = new BufferedReader(new InputStreamReader(CLIENTE_SOCKET.getInputStream()));
            
            ENVIA.println("999");
            
            // Recebendo dados dos candidatos do servidor para armazenar na memória
            qtd = Integer.parseInt(RECEBE.readLine());
            Candidatos.clear();
            for(int i=0; i<qtd; i++){
                cod = Integer.parseInt(RECEBE.readLine());
                nome = RECEBE.readLine();
                partido = RECEBE.readLine();
                Integer.parseInt(RECEBE.readLine());
                numVotos = 0;
                Candidatos.add(new Candidato(cod, nome, partido, numVotos)); // Inserindo candidato no vetor de candidatos em meória
                s += cod + " - "+ nome + " (" + partido + ")\n";
            }
            Integer.parseInt(RECEBE.readLine()); // Recebendo quantidade de votos brancos apenas para seguir protocolo de comunicação
            brancos = 0;
            Integer.parseInt(RECEBE.readLine()); // Recebendo quantidade de votos brancos apenas para seguir protocolo de comunicação
            nulos = 0;
            ja.setText(s);
            
            CLIENTE_SOCKET.close();
            
        } catch (IOException ex) {
            flagErro = 1;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(flagErro == 1) return false;
            return true;
        }
    } 
    
    /*Função que incrementa o voto do candidato escolhido*/
    public boolean computaVoto(String cod) {
        int qtd;
        
        qtd = Candidatos.size();
        
        // percorrendo todos os candidatos para computar voto
        for (int i = 0; i<qtd; i++){
            if (Candidatos.get(i).getCodigoVotacao() == Integer.parseInt(cod)){
                Candidatos.get(i).incNumVotos();
                return true;
            }else if(Integer.parseInt(cod) == 2){
                brancos += 1;
                return true;
            }else if(Integer.parseInt(cod) == 3){
                nulos += 1;
                return true;
            }
        }
        return false;
    }
    
    /*Função que abre conexão com servidor para receber os dados dos candidatos*/
    public boolean enviaVotacao() {
        int qtd, flagErro = 0;
        
        try {
            //Abrindo conexão com servidor
            CLIENTE_SOCKET = new Socket(ip, porta);
            //Preparando manipuladore de Stream de dados
            ENVIA = new PrintWriter(CLIENTE_SOCKET.getOutputStream(), true);
            RECEBE = new BufferedReader(new InputStreamReader(CLIENTE_SOCKET.getInputStream()));
            
            ENVIA.println("888");
            
            qtd = Candidatos.size();
            
            // Enviando o código e o número de votos de cada candidato em memória
            for(int i=0; i<qtd; i++){
                ENVIA.println(String.valueOf(Candidatos.get(i).getCodigoVotacao()));
                ENVIA.println(String.valueOf(Candidatos.get(i).getNumVotos()));
            }
            // Enviando a quantidade de votos brancos e nulos
            ENVIA.println(String.valueOf(brancos));
            ENVIA.println(String.valueOf(nulos));
            
            CLIENTE_SOCKET.close();
            
        } catch (IOException ex) {
            flagErro = 1;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(flagErro == 1) return false;
            return true;
        }
    }
    
    /*Função que abre conexão com servidor para receber os dados dos candidatos*/
        /*Semelhante ao recebeCandidatos, mas armazena a quantidade de votos branco es nulos recebidos do servidor*/
    public boolean exibeParciais(JTextArea ja) {
        String s ="", nome, partido;
        int qtd, cod, numVotosTemp, flagErro = 0;
        
        try {

            CLIENTE_SOCKET = new Socket(ip, porta);
            ENVIA = new PrintWriter(CLIENTE_SOCKET.getOutputStream(), true);
            RECEBE = new BufferedReader(new InputStreamReader(CLIENTE_SOCKET.getInputStream()));
            
            ENVIA.println("999");
            
            qtd = Integer.parseInt(RECEBE.readLine());
            for(int i=0; i<qtd; i++){
                cod = Integer.parseInt(RECEBE.readLine());
                nome = RECEBE.readLine();
                partido = RECEBE.readLine();
                numVotosTemp = Integer.parseInt(RECEBE.readLine());
                s += nome + " (" + partido + ") - "+ numVotosTemp+ " votos\n";
            }
            brancosTemp = Integer.parseInt(RECEBE.readLine());
            s += "brancos - "+ brancosTemp + " votos\n";
            nulosTemp = Integer.parseInt(RECEBE.readLine());
            s += "nulos - "+ nulosTemp + " votos\n";
            ja.setText(s);
            
            CLIENTE_SOCKET.close();
            
        } catch (IOException ex) {
            flagErro = 1;
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(flagErro == 1) return false;
            return true;
        }
    }
}   
