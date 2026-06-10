package com.example.appcademia

// -------- IMPORTS --------
import android.content.Context
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

// -------- MODELOS --------
data class Exercicio(
    val nome: String,
    val peso: String,
    val series: String
)

data class TreinoDia(
    val dia: String,
    val data: String,
    val exercicios: MutableList<Exercicio>
)

data class Registro(
    val data: String,
    val peso: String,
    val peito: String,
    val braco: String,
    val costas: String,
    val perna: String,
    val coxa: String
)

// -------- DATA --------
fun gerarData(diaTexto: String): String {

    val mapa = mapOf(
        "domingo" to Calendar.SUNDAY,
        "segunda" to Calendar.MONDAY,
        "terça" to Calendar.TUESDAY,
        "terca" to Calendar.TUESDAY,
        "quarta" to Calendar.WEDNESDAY,
        "quinta" to Calendar.THURSDAY,
        "sexta" to Calendar.FRIDAY,
        "sabado" to Calendar.SATURDAY,
        "sábado" to Calendar.SATURDAY
    )

    val cal = Calendar.getInstance()
    val entrada = diaTexto.lowercase().split(" ")[0]
    val alvo = mapa[entrada] ?: return ""

    while (cal.get(Calendar.DAY_OF_WEEK) != alvo) {
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }

    return SimpleDateFormat("dd/MM", Locale("pt","BR")).format(cal.time)
}

fun dataHoje(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale("pt","BR")).format(Date())
}

// -------- SALVAR --------
fun salvarTreinos(context: Context, lista: List<TreinoDia>) {
    val prefs = context.getSharedPreferences("treinos", Context.MODE_PRIVATE)
    prefs.edit().putString("dados", Gson().toJson(lista)).apply()
}

fun carregarTreinos(context: Context): MutableList<TreinoDia> {
    val json = context.getSharedPreferences("treinos", Context.MODE_PRIVATE)
        .getString("dados", null)

    return if (json != null) {
        val type = object : TypeToken<MutableList<TreinoDia>>() {}.type
        Gson().fromJson(json, type)
    } else mutableListOf()
}

fun salvarProgresso(context: Context, lista: List<Registro>) {
    val prefs = context.getSharedPreferences("progresso", Context.MODE_PRIVATE)
    prefs.edit().putString("dados", Gson().toJson(lista)).apply()
}

fun carregarProgresso(context: Context): MutableList<Registro> {
    val json = context.getSharedPreferences("progresso", Context.MODE_PRIVATE)
        .getString("dados", null)

    return if (json != null) {
        val type = object : TypeToken<MutableList<Registro>>() {}.type
        Gson().fromJson(json, type)
    } else mutableListOf()
}

// -------- MAIN --------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppPrincipal() }
    }
}

// -------- MENU --------
@Composable
fun AppPrincipal() {

    var tela by remember { mutableStateOf("treino") }

    Column {

        Spacer(Modifier.height(15.dp))

        Row(Modifier.fillMaxWidth().padding(8.dp)) {

            Button({ tela = "treino" }, Modifier.weight(1f)) { Text("Treino") }
            Spacer(Modifier.width(6.dp))

            Button({ tela = "historico" }, Modifier.weight(1f)) { Text("Histórico") }
            Spacer(Modifier.width(6.dp))

            Button({ tela = "progresso" }, Modifier.weight(1f)) { Text("Progresso") }
        }

        when (tela) {
            "treino" -> TelaTreino()
            "historico" -> TelaHistorico()
            "progresso" -> TelaProgresso()
        }
    }
}

// -------- TREINO --------
@Composable
fun TelaTreino() {

    val context = LocalContext.current

    var dia by remember { mutableStateOf("") }
    var nome by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }

    val lista = remember { mutableStateListOf<TreinoDia>() }
    var atual by remember { mutableStateOf<TreinoDia?>(null) }

    LaunchedEffect(Unit) {
        lista.clear()
        lista.addAll(carregarTreinos(context))
    }

    LazyColumn(Modifier.padding(16.dp)) {

        item {
            Spacer(Modifier.height(40.dp))

            Text("TREINO")

            TextField(value = dia, onValueChange = { dia = it }, label = { Text("Dia") })

            Text("Data: ${gerarData(dia)}")

            Button(onClick = {
                if (dia.isNotEmpty()) {
                    val novo = TreinoDia(dia, gerarData(dia), mutableListOf())
                    lista.add(novo)
                    atual = novo
                    salvarTreinos(context, lista)
                }
            }) { Text("Criar") }
        }

        item {
            if (atual != null) {

                TextField(value = nome, onValueChange = { nome = it }, label = { Text("Exercício") })
                TextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso") })
                TextField(value = series, onValueChange = { series = it }, label = { Text("Séries") })

                Button(onClick = {
                    atual?.exercicios?.add(Exercicio(nome, peso, series))
                    salvarTreinos(context, lista)
                    nome = ""; peso = ""; series = ""
                }) { Text("Adicionar") }
            }
        }

        itemsIndexed(lista) { index, treino ->

            Card(Modifier.padding(6.dp)) {
                Column(Modifier.padding(10.dp)) {

                    Text("${treino.dia} - ${treino.data}")

                    treino.exercicios.forEach {
                        Text("${it.nome} - ${it.peso}kg - ${it.series} séries")
                    }

                    Button(onClick = {
                        lista.removeAt(index)
                        salvarTreinos(context, lista)
                    }) { Text("Remover") }
                }
            }
        }
    }
}

// -------- HISTÓRICO --------
@Composable
fun TelaHistorico() {

    val context = LocalContext.current
    val lista = remember { mutableStateListOf<TreinoDia>() }

    LaunchedEffect(Unit) {
        lista.clear()
        lista.addAll(carregarTreinos(context))
    }

    LazyColumn(Modifier.padding(16.dp)) {

        item {
            Spacer(Modifier.height(40.dp))
            Text("HISTÓRICO")
        }

        itemsIndexed(lista) { index, treino ->

            Card(Modifier.padding(6.dp)) {
                Column(Modifier.padding(10.dp)) {

                    Text("${treino.dia} - ${treino.data}")

                    treino.exercicios.forEach {
                        Text("${it.nome} - ${it.peso}kg - ${it.series} séries")
                    }

                    Button(onClick = {
                        lista.removeAt(index)
                        salvarTreinos(context, lista)
                    }) { Text("Remover") }
                }
            }
        }
    }
}

// -------- PROGRESSO --------
@Composable
fun TelaProgresso() {

    val context = LocalContext.current

    var data by remember { mutableStateOf(dataHoje()) }
    var peso by remember { mutableStateOf("") }
    var peito by remember { mutableStateOf("") }
    var braco by remember { mutableStateOf("") }
    var costas by remember { mutableStateOf("") }
    var perna by remember { mutableStateOf("") }
    var coxa by remember { mutableStateOf("") }

    val lista = remember { mutableStateListOf<Registro>() }

    LaunchedEffect(Unit) {
        lista.clear()
        lista.addAll(carregarProgresso(context))
    }

    Column(Modifier.padding(16.dp)) {

        Text("PROGRESSO")

        TextField(value = data, onValueChange = { data = it }, label = { Text("Data") })
        TextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso") })
        TextField(value = peito, onValueChange = { peito = it }, label = { Text("Peito") })
        TextField(value = braco, onValueChange = { braco = it }, label = { Text("Braço") })
        TextField(value = costas, onValueChange = { costas = it }, label = { Text("Costas") })
        TextField(value = perna, onValueChange = { perna = it }, label = { Text("Perna") })
        TextField(value = coxa, onValueChange = { coxa = it }, label = { Text("Coxa") })

        Button(onClick = {
            lista.add(Registro(data, peso, peito, braco, costas, perna, coxa))
            salvarProgresso(context, lista)

            peso = ""; peito = ""; braco = ""; costas = ""; perna = ""; coxa = ""
        }) { Text("Salvar") }

        LazyColumn {
            itemsIndexed(lista) { index, item ->

                Card(Modifier.padding(6.dp)) {
                    Column(Modifier.padding(10.dp)) {

                        Text("Data: ${item.data}")
                        Text("Peso: ${item.peso}")
                        Text("Peito: ${item.peito}")
                        Text("Braço: ${item.braco}")
                        Text("Costas: ${item.costas}")
                        Text("Perna: ${item.perna}")
                        Text("Coxa: ${item.coxa}")

                        Button(onClick = {
                            lista.removeAt(index)
                            salvarProgresso(context, lista)
                        }) { Text("Remover") }
                    }
                }
            }
        }
    }
}