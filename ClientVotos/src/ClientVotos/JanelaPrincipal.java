/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ClientVotos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @authors Rafael, Gabriel e Alair
 *
 */
public class JanelaPrincipal {

    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JPanel PainelCandidatos;
    JPanel PainelVotacao;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JLabel jtRotulo, jtRotulo2, jtRotulo3;
    JTabbedPane tabbedPane;
    JPanel pPainelDeExibicaoDeDados;
    JTable jt;
    JPanel pPainelDeInsecaoDeDados;
    JPanel pPainelDeDDL;
    JButton bt;
    JButton bt2, bt3, bt4;
    String usuario, senha;
    JTextArea ja = new JTextArea(), ja2 = new JTextArea();
    Client cliente = new Client();
    JTextField campo;
    int nVotos = 0;

    public void ExibeJanelaPrincipal() {
        /*Janela*/
        j = new JFrame("ICMC-USP - Rede de Computadores - Urna Eletrônica");
        j.setSize(430, 300);
        j.setLayout(new BorderLayout());
        //j.setLocationRelativeTo(null);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte inferior (south) - com área de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setEditable(false);
        jtAreaDeStatus.setText("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);
        

        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

 
       
        /*Tab de Votacao*/
            /*Area de listagem dos candidatos*/
        JPanel pPainelGeral = new JPanel();
        pPainelGeral.setLayout(new BorderLayout());
        PainelCandidatos = new JPanel();
        PainelCandidatos.setLayout(new BorderLayout());
        JPanel painelBt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        PainelCandidatos.add(painelBt, BorderLayout.SOUTH);
        bt = new JButton("Carregar Lista");
        painelBt.add(bt);
        ja.setEditable(false);
        JScrollPane jsp = new JScrollPane(ja);
        PainelCandidatos.add(jsp, BorderLayout.CENTER);
        
            /*Area de votação*/
        JPanel painelGeralVotacao = new JPanel(new BorderLayout());
        PainelVotacao = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();        
        
        jtRotulo = new JLabel();
        jtRotulo.setText("Digite o número do candidato ou:");
        gbc.insets = new Insets(5, 10, 0, 10);
        gbc.gridx = 0;
	gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        PainelVotacao.add(jtRotulo,gbc);
        
        jtRotulo2 = new JLabel();
        jtRotulo2.setText("2 - Para votar BRANCO.");
	gbc.gridy = 1;
        PainelVotacao.add(jtRotulo2,gbc);
        
        jtRotulo3 = new JLabel();
        jtRotulo3.setText("3 - Para votar NULO.");
	gbc.gridy = 2;
        PainelVotacao.add(jtRotulo3,gbc);
        
        campo = new JTextField();
        campo.setEnabled(false);
	gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        PainelVotacao.add(campo,gbc);
        
        bt2 = new JButton("votar");
        bt2.setEnabled(false);
	gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        PainelVotacao.add(bt2,gbc);
        
        bt3 = new JButton("Encerrar Votação/ Enviar");
        bt3.setEnabled(false);
        
        painelGeralVotacao.add(PainelVotacao,BorderLayout.NORTH);
        painelGeralVotacao.add(bt3,BorderLayout.SOUTH);
        pPainelGeral.add(PainelCandidatos, BorderLayout.EAST);
        pPainelGeral.add(painelGeralVotacao, BorderLayout.WEST);
        tabbedPane.add(pPainelGeral, "Votacao");
        
        
        /*Tab de Parciais do servidor*/
        pPainelDeDDL = new JPanel();
        pPainelDeDDL.setLayout(new BorderLayout());
        ja2.setEditable(false);
        JScrollPane jsp2 = new JScrollPane(ja2);
        pPainelDeDDL.add(jsp2, BorderLayout.CENTER);
        bt4 = new JButton("Atualizar parciais");
        JPanel PainelBotao2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        PainelBotao2.add(bt4);
        pPainelDeDDL.add(PainelBotao2, BorderLayout.SOUTH);
        tabbedPane.add(pPainelDeDDL, "Parciais do Servidor");
        
        this.DefineEventos();        
        
        j.setVisible(true);
    }

    private void DefineEventos() {
       
       /*Ação para o botão de carregar lista*/
       bt.addActionListener(
            new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent ae) {
               if(cliente.recebeCandidatos(ja)){
                   jtAreaDeStatus.setBackground(Color.green);
                   jtAreaDeStatus.setText("Votação habilitada");              
                   campo.setEnabled(true); // Habilita a inserção de códigos de votação
                   campo.requestFocus(); //Muda o cursor para o campo de votação
                   bt2.setEnabled(true);
                   bt.setEnabled(false);
               }else{
                   jtAreaDeStatus.setBackground(Color.red);
                   jtAreaDeStatus.setText("Problemas de conexão com servidor");
               }
           }
                
        });
       
       /*Ação para o botão de votar*/
       bt2.addActionListener(
            new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent ae) {
               if(cliente.computaVoto(campo.getText())){
                   jtAreaDeStatus.setBackground(Color.white);
                   nVotos += 1;
                   jtAreaDeStatus.setText(nVotos + "º voto computado");              
                   bt3.setEnabled(true);
                   campo.setText("");
               }else{
                   jtAreaDeStatus.setBackground(Color.red);
                   jtAreaDeStatus.setText("Código errado, voto não computado");
               }
               
           }
                
        });
       
       /*Ação para o botão Encerrar/Enviar votos */
       bt3.addActionListener(
            new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent ae) {
               if(cliente.enviaVotacao()){
                   jtAreaDeStatus.setBackground(Color.orange);
                   jtAreaDeStatus.setText("Votação encerrada");              
                   bt.setEnabled(false);
                   bt2.setEnabled(false);
                   bt3.setEnabled(false);
               }else{
                   jtAreaDeStatus.setBackground(Color.red);
                   jtAreaDeStatus.setText("Erro na comunicação com servidor");
               }               
           }
                
        });
       
       /*Ação para o botção de parciais do servidor*/
       bt4.addActionListener(
            new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent ae) {
               if(cliente.exibeParciais(ja2)){
                    jtAreaDeStatus.setBackground(Color.white);
                    jtAreaDeStatus.setText("Parciais atualizadas");              
               }else{
                   jtAreaDeStatus.setBackground(Color.red);
                   jtAreaDeStatus.setText("Problemas de conexão com servidor");
               }
           }
                
        });
       
       /*Ação para o campo de texto para responder ao apertar enter*/
       campo.addActionListener(
            new ActionListener(){
           @Override
           public void actionPerformed(ActionEvent ae) {
               if(cliente.computaVoto(campo.getText())){
                   jtAreaDeStatus.setBackground(Color.white);
                   nVotos += 1;
                   jtAreaDeStatus.setText(nVotos + "º voto computado");              
                   bt3.setEnabled(true);
                   campo.setText("");
               }else{
                   jtAreaDeStatus.setBackground(Color.red);
                   jtAreaDeStatus.setText("Código errado, voto não computado");
               }
           }
                
        });
       
    }
}
