/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servervotos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.exit;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael Pereira
 */
public class ThreadControler extends Thread{
    String resposta;
    ServerSocket OUVIDO;

    public ThreadControler(ServerSocket OUVIDO) {
        this.OUVIDO = OUVIDO;
    }
            
    public void run() {
        try {
            BufferedReader LEITOR_ENTRADA_PADRAO = new BufferedReader(new InputStreamReader(System.in));
            do{
                    resposta = LEITOR_ENTRADA_PADRAO.readLine();
            }while(resposta.compareTo("close") != 0 && resposta.compareTo("CLOSE") != 0);
            OUVIDO.close();
            exit (0);
        } catch (IOException ex) {
            Logger.getLogger(ThreadControler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
}
