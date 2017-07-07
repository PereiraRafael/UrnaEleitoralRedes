// Servidor de votos com suporte para múltiplas conexões via threads

#include<stdio.h>
#include<string.h>    //strlen
#include<stdlib.h>    
#include<sys/socket.h>
#include<arpa/inet.h> //inet_addr
#include<unistd.h>    //write
#include<pthread.h> //para threading , linkar com lpthread
#include <string.h>

typedef struct{
	int codigo_votacao;
	char nome_candidato[30];
	char partido[30];
	int num_votos;
} Candidato;
	
FILE *arq;

pthread_mutex_t lock;

//função para manipular thread
void *connection_handler(void *);

int enviarLista(int);

int receberVotos(int);

void insereQuebraLinha(char *);

//função substituta do recv()
void myRecv(int, char *, int, int);
	
int main(int argc , char *argv[])
{
    int socket_desc , client_sock , sc , *new_sock;
    struct sockaddr_in server , client;

	//Inbicializar candidatos caso não tenha arquivo salvo
	if ((arq = fopen("candidatos.bin","rb"))==NULL){
		Candidato c[3];
		
		c[0].codigo_votacao = 13;
		strcpy(c[0].nome_candidato,"Lula");
		strcpy(c[0].partido,"PT");
		c[0].num_votos = 0;
		
		c[1].codigo_votacao = 30;
		strcpy(c[1].nome_candidato,"Eneas");
		strcpy(c[1].partido,"PRONA");
		c[1].num_votos = 0;
		
		c[2].codigo_votacao = 45;
		strcpy(c[2].nome_candidato,"Aecio");
		strcpy(c[2].partido,"PSDB");
		c[2].num_votos = 0;
		
		if ((arq = fopen("candidatos.bin","wb"))==NULL){
			printf("Erro ao criar arquivo");
			return 1;
		}
		int zero = 0, ncandidatos = 3;
		fwrite(&ncandidatos,sizeof(int),1,arq);
		fwrite(c,sizeof(Candidato),3,arq);
		fwrite(&zero,sizeof(int),1,arq);
		fwrite(&zero,sizeof(int),1,arq);
	
		fclose(arq);
	}
	
	//Preparando mutex para sincronização de thread
	if (pthread_mutex_init(&lock, NULL) != 0)
    {
        printf("\n inicializacao do Mutex falhou\n");
        return 1;
    }

    //Criação de socket
    socket_desc = socket(AF_INET , SOCK_STREAM , 0);
    if (socket_desc == -1)
    {
        printf("Nao foi possível criar socket");
        return 1;
    }
    puts("Socket criado");

    //Preparando sockaddr_in structure
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons( 40008 );

    //Vincular socket
    if( bind(socket_desc,(struct sockaddr *)&server , sizeof(server)) < 0)
    {
        perror("Vinculacao falhou. Erro");
        return 1;
    }
    puts("Vinculacao feita");

    //Ouvir
    listen(socket_desc , 3);

    //Aceitando futuras conexoes
    puts("Esperando por novas conexoes...");
    sc = sizeof(struct sockaddr_in);

	while(client_sock=accept(socket_desc,(struct sockaddr*)&client,(socklen_t*)&sc))
    {
    	puts("Conexao aceita");

        pthread_t sniffer_thread;
        new_sock = (void *)malloc(1);
        *new_sock = client_sock;
		
		// Criação de thread para tratar conexão aceita
        if( pthread_create( &sniffer_thread , NULL ,  connection_handler , (void*) new_sock) < 0)
        {
            perror("Nao foi possivel criar thread");
            return 1;
        }

        puts("Manipulador de conexao atribuido");
    }

    if (client_sock < 0)
    {
        perror("Aceitacao falhou");
        return 1;
    }
    
    // destruindo mutex
    pthread_mutex_destroy(&lock);
    return 0;
}
/*
  Esta funcao irá manipular a conexão de cada cliente
  */
void *connection_handler(void *socket_desc)
{
    //Obter o descritor do socket
    int sock = *(int*)socket_desc;
    int n;
	char opt[6];

	// Receber opcao do cliente
    myRecv(sock,opt,6,0);
    n = atoi(opt);
	switch(n){
		case 999:
			puts("Opcao 999 escolhida");
			enviarLista(sock);
		  	break;
		case 888:
			puts("Opcao 888 escolhida");
			receberVotos(sock);
			break;	
		default:
			close(sock);
			puts("Conexao encerrada");
			break;  	
	}
	  
}


int enviarLista(int sock){
	int qtd, brancos, nulos, i;
	Candidato *c;
	char  client_message[30];
	
	if ((arq = fopen("candidatos.bin","rb"))==NULL){
		printf("Erro ao criar arquivo");
		return 1;
	}
	// lendo do arquivo a quantidade de candidatos
	fread(&qtd,sizeof(int),1,arq);
	
	// Alocando um vetor de candidatos
	c =(Candidato *) calloc(qtd, sizeof(Candidato));
	
	// Lendo todos os candidatos do arquivo para o vetor de candidatos
	fread(c,sizeof(Candidato), qtd, arq);
	// Lendo a quantidade de brancos e nulos do arquivo
	fread(&brancos,sizeof(int),1,arq);
	fread(&nulos,sizeof(int),1,arq);
	fclose(arq);
	
	// Envio de daodos na forma de string/char[] para a aplicação cliente
	sprintf(client_message, "%d", qtd); //Convertendo int(qtd) para string(client_message)
	insereQuebraLinha(client_message);  // inserindo quebra de linha '\n' ao final da string
	send(sock,client_message,strlen(client_message),0);
	for (i=0; i<qtd; i++){
		sprintf(client_message, "%d", c[i].codigo_votacao);
		insereQuebraLinha(client_message);
		send(sock,client_message,strlen(client_message),0);
		strcpy(client_message, c[i].nome_candidato);
		insereQuebraLinha(client_message);
		send(sock,client_message,strlen(client_message),0);
		strcpy(client_message, c[i].partido);
		insereQuebraLinha(client_message);
		send(sock,client_message,strlen(client_message),0);
		sprintf(client_message, "%d", c[i].num_votos);
		insereQuebraLinha(client_message);
		send(sock,client_message,strlen(client_message),0);
	}
	sprintf(client_message, "%d", brancos);
	insereQuebraLinha(client_message);
	send(sock,client_message,strlen(client_message),0);
	sprintf(client_message, "%d", nulos);
	insereQuebraLinha(client_message);
	send(sock,client_message,strlen(client_message),0);
	
	close(sock);
	puts("Conexao encerrada");
	return 0;

}

int receberVotos(int sock){
	int qtd, brancos, nulos, brancosAtualizados, nulosAtualizados,  i, j;
	Candidato *c, *cAtualizado;
	char client_message[30];
	
	// Ativacao da trava /mutex/ para sincronização da thread
	pthread_mutex_lock(&lock);
	puts("Atualizando votos...");
	
	if ((arq = fopen("candidatos.bin","rb"))==NULL){
		printf("Erro ao abrir arquivo");
		return 1;
	}
	fread(&qtd,sizeof(int),1,arq);
		
	// Criacao de vetor de candidatos para armazenar dados do arquivo
	c =(Candidato *) calloc(qtd, sizeof(Candidato));
	
	// Criação de vetor de candidatos para receber os dados de voto do cliente
	cAtualizado =(Candidato *) calloc(qtd, sizeof(Candidato));
	
	// Preenchimento dos dados de votos de acordo com o arquivo	
	fread(c,sizeof(Candidato), qtd, arq);
	fread(&brancos,sizeof(int),1,arq);
	fread(&nulos,sizeof(int),1,arq);
	fclose(arq);
		
	// Armazenamento dos dados de voto do cliente (código e voto de cada candidato + brancos e nulos)
	for (i=0; i<qtd; i++){
		myRecv(sock,client_message,30,0);
		cAtualizado[i].codigo_votacao = atoi(client_message);
		myRecv(sock,client_message,30,0);
		cAtualizado[i].num_votos = atoi(client_message);			
	}
	myRecv(sock,client_message,30,0);
	brancosAtualizados = atoi(client_message);
	myRecv(sock,client_message,30,0);
	nulosAtualizados = atoi(client_message);
		
	// Contabilização dos votos recebidos com os votos já salvos	
	for (i=0; i<qtd; i++){
		for (j=0; j<qtd; j++){
			if(cAtualizado[j].codigo_votacao == c[i].codigo_votacao){
				c[i].num_votos += cAtualizado[j].num_votos;
			}
		}			
	}
	brancos += brancosAtualizados;
	nulos += nulosAtualizados;
		
	// Salvamento dos votos atualizados em arquivo	
	if ((arq = fopen("candidatos.bin","wb"))==NULL){
		printf("Erro ao criar arquivo");
		return 1;
	}
	fwrite(&qtd,sizeof(int),1,arq);
	fwrite(c,sizeof(Candidato),qtd,arq);
	fwrite(&brancos,sizeof(int),1,arq);
	fwrite(&nulos,sizeof(int),1,arq);
		
	fclose(arq);
		
	// Liberaçao da trava /mutex/, outra thread já pode entrar neste trecho de código
	pthread_mutex_unlock(&lock);
	puts("Votos atualizados");
		
	close(sock);
	puts("Conexao encerrada");
	return 0;
}

/**
* Função para inserir quebra de linha ao final de uma string.
*/
void insereQuebraLinha(char *string){
	int tamanho = strlen(string);
	string[tamanho] = '\n';
	string[tamanho+1] = '\0';	
}


/**
* Função modificada do recv para controlar o recebimento de Strings.
* Verifica a ocorrência de \n ou \0 para finalizar o recebimento
*/
void myRecv(int sock,char *client_message, int lenght, int n){
	char buffer[1];
	int bytes, i = 0;
	
	do{
		bytes = recv(sock,buffer,1,0);
		client_message[i] = buffer[0];
		i++;
	} while(buffer[0] != '\n' && i <= lenght && bytes != 0 && buffer[0] != '\0');
	client_message[i-1] = '\0';
}
