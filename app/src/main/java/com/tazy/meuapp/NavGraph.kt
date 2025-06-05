package com.tazy.meuapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meuapp.ui.telas.TelaConexoes

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onCriarConta = {
                    navController.navigate("cadastro")
                },
                onLoginSucesso = {
                    navController.navigate("lobbyPrincipal") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("cadastro") {
            CadastroScreen(
                onCadastroFinalizado = {
                    navController.popBackStack()
                },
                onCadastroPrimeiroLogin = {
                    navController.navigate("loginPrimeiraVez") {
                        popUpTo("cadastro") { inclusive = true }
                    }
                }
            )
        }

        composable("loginPrimeiraVez") {
            LoginPrimeiraVez(navController = navController, modoEdicao = false)
        }

        composable("loginPrimeiraVez/editar") {
            LoginPrimeiraVez(navController = navController, modoEdicao = true)
        }

        composable("telaPrincipalGrupos") {
            TelaPrincipalGrupos(navController)
        }

        composable("chat/{grupoId}") { backStackEntry ->
            val grupoId = backStackEntry.arguments?.getString("grupoId") ?: return@composable
            ChatScreen(navController, grupoId)
        }

        composable("criarGrupo") {
            CriarGrupoScreen(navController)
        }

        composable("lobbyPrincipal") {
            TelaLobbyPrincipal(navController)
        }
        composable("telaFeedNoticias") {
            TelaFeedNoticias(navController = navController)
        }
        composable("telaConexoes") {
            TelaConexoes(navController = navController)
        }
        composable(
            route = "chat/{matchId}",
            arguments = listOf(navArgument("matchId") { type = NavType.StringType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            TelaChatIndividual(
                navController = navController,
                matchId = matchId
            )
        }
    }
}
