# Norte

App para ajudar pessoas com muitas vontades (viajar, intercâmbio, shows, carreira etc) a decidir o que priorizar agora, via cadastro + questionário adaptativo + recomendação gerada por IA.

## Estrutura

```
norte-app/
  backend/    Java 21 + Spring Boot 3 + H2 (arquivo local) + Groq API
  frontend/   React + Vite + Tailwind
```

## Como rodar

### 1. Backend

Pré-requisitos: Java 21 e Maven instalados.

```bash
cd backend
export GROQ_API_KEY=sua_chave_aqui   # crie de graça em https://console.groq.com/keys
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. O banco H2 fica salvo em `backend/data/norte.mv.db` (persiste entre reinícios). Console do H2 em `http://localhost:8080/h2-console`.

### 2. Frontend

Pré-requisitos: Node.js 18+.

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Sobe em `http://localhost:5173`.

## Fluxo implementado

1. Cadastro/login (senha com hash BCrypt, token de sessão salvo em tabela própria, expira em 30 dias)
2. Usuário lista suas vontades (`/goals`) — pode adicionar e remover a qualquer momento
3. Questionário gerado a partir das vontades ativas atuais (6 perguntas gerais + perguntas específicas por categoria: viagem, intercâmbio, show, carreira, saúde, financeiro, aprendizado)
4. Ao responder, a IA (Groq, gratuita) gera a recomendação com base num framework de clarificação de valores, motivação intrínseca x extrínseca, matriz urgência x importância
5. Cada resultado é salvo com número de versão — o histórico completo fica disponível na Dashboard
6. Ao atualizar as vontades (adicionar/remover), um novo questionário + nova recomendação (nova versão) refletem o "Norte" atualizado, sem apagar o anterior

## Por que ficou assim (e o que evitei fazer agora)

**Por que Groq e não a varredura em tempo real na web que você mencionou originalmente?**
Buscar "opiniões de psicólogos" na internet a cada requisição seria lento, caro e difícil de tornar consistente — os resultados variam a cada busca e é difícil garantir que o conteúdo encontrado seja confiável. Em vez disso, fixei o framework psicológico (valores, motivação, urgência x importância) diretamente no prompt do sistema, curado uma vez, e deixei a IA aplicar esse framework aos dados da pessoa. É mais previsível, mais barato e mais fácil de testar/depurar. Se no futuro você quiser buscas reais, dá pra adicionar uma etapa de web search antes da chamada à IA — mas comece validando o framework fixo primeiro.

**Por que não JWT "de verdade"?**
Um token opaco salvo no banco (com expiração) resolve o mesmo problema com muito menos superfície de erro do que configurar Spring Security + assinatura JWT + refresh tokens. Para uma v1 que ainda está sendo validada, isso é deliberado — não é economia de qualidade, é adiar complexidade que só vale a pena depois que o produto provar que vai pra frente.
