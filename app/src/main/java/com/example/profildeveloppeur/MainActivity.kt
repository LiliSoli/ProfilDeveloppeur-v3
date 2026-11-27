package com.example.profildeveloppeur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.profildeveloppeur.ui.theme.ProfilDeveloppeurTheme
import androidx.compose.material.icons.Icons
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProjectUi(
    val id: Int,
    val title: String,
    val description: String,
    val technologies: List<String>
)

fun sampleProjects(): List<ProjectUi> {
    return listOf(
        ProjectUi(
            id = 1,
            title = "GreenCork",
            description = "Application web B2B pour un importateur de vins",
            technologies = listOf("Spring Boot", "Next.js")
        ),
        ProjectUi(
            id = 2,
            title = "Notae",
            description = "Projet de groupe réalisé pendant la formation CDA",
            technologies = listOf("Spring Boot", "Next.js")
        ),
        ProjectUi(
            id = 3,
            title = "Profil développeur",
            description = "Application Android avec Jetpack Compose",
            technologies = listOf("Kotlin", "Jetpack Compose")
        ),
        ProjectUi(
            id = 4,
            title = "Product Explorer",
            description = "Catalogue de produits Android réalisé en TP",
            technologies = listOf("Kotlin", "Jetpack Compose", "ViewModel")
        ),
        ProjectUi(
            id = 5,
            title = "Migration MySQL vers MongoDB",
            description = "Batch de migration de données réalisé en alternance",
            technologies = listOf("Java", "Spring Batch", "MySQL", "MongoDB")
        )
    )
}

fun sampleSkills(): List<String> {
    return listOf("Java", "Spring Boot", "React", "Kotlin", "Compose")
}

data class ProfileUiState(
    val projects: List<ProjectUi> = emptyList(),
    val searchQuery: String = "",
    val showOnlyFavorites: Boolean = false,
    val favoriteProjectIds: List<Int> = emptyList()
)

class ProfileViewModel : ViewModel() {

    private val allProjects: List<ProjectUi> = sampleProjects()

    private val _uiState = MutableStateFlow(
        ProfileUiState(projects = allProjects)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private fun filterProjects(
        searchQuery: String,
        showOnlyFavorites: Boolean,
        favoriteProjectIds: List<Int>
    ): List<ProjectUi> {
        return allProjects.filter { project ->
            val matchesSearch =
                project.title.contains(searchQuery, ignoreCase = true) ||
                        project.description.contains(searchQuery, ignoreCase = true) ||
                        project.technologies.any { technology ->
                            technology.contains(searchQuery, ignoreCase = true)
                        }
            val matchesFavorite =
                !showOnlyFavorites || favoriteProjectIds.contains(project.id)
            matchesSearch && matchesFavorite
        }
    }

    fun onSearchQueryChange(newValue: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = newValue,
                projects = filterProjects(
                    searchQuery = newValue,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProjectIds = currentState.favoriteProjectIds
                )
            )
        }
    }

    fun onToggleFavoriteFilter() {
        _uiState.update { currentState ->
            val newShowOnlyFavorites = !currentState.showOnlyFavorites
            currentState.copy(
                showOnlyFavorites = newShowOnlyFavorites,
                projects = filterProjects(
                    searchQuery = currentState.searchQuery,
                    showOnlyFavorites = newShowOnlyFavorites,
                    favoriteProjectIds = currentState.favoriteProjectIds
                )
            )
        }
    }

    fun onFavoriteClick(projectId: Int) {
        _uiState.update { currentState ->
            val newFavoriteIds =
                if (currentState.favoriteProjectIds.contains(projectId)) {
                    currentState.favoriteProjectIds - projectId
                } else {
                    currentState.favoriteProjectIds + projectId
                }
            currentState.copy(
                favoriteProjectIds = newFavoriteIds,
                projects = filterProjects(
                    searchQuery = currentState.searchQuery,
                    showOnlyFavorites = currentState.showOnlyFavorites,
                    favoriteProjectIds = newFavoriteIds
                )
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfilDeveloppeurTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfileContainer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContainer(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        projects = uiState.projects,
        searchQuery = uiState.searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        showOnlyFavorites = uiState.showOnlyFavorites,
        onToggleFavoriteFilter = viewModel::onToggleFavoriteFilter,
        favoriteProjectIds = uiState.favoriteProjectIds,
        onFavoriteClick = viewModel::onFavoriteClick,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    projects: List<ProjectUi>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyFavorites: Boolean,
    onToggleFavoriteFilter: () -> Unit,
    favoriteProjectIds: List<Int>,
    onFavoriteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileCard(
                    name = "Aurélie S.",
                    role = "Développeuse Java",
                    description = "En alternance chez Worldline",
                    skills = sampleSkills(),
                    isAvailable = true,
                    email = "aurelie.s@email.com",
                    location = "Tours, France",
                    onContactClick = {
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    text = "Mes projets",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ProjectSearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(onClick = onToggleFavoriteFilter) {
                    Text(
                        text = if (showOnlyFavorites) {
                            "Afficher tous les projets"
                        } else {
                            "Afficher uniquement les favoris"
                        }
                    )
                }
            }
            item {
                Text(
                    text = if (projects.size <= 1) {
                        "${projects.size} projet affiché"
                    } else {
                        "${projects.size} projets affichés"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(
                items = projects,
                key = { project -> project.id }
            ) { project ->
                ProjectCard(
                    project = project,
                    isFavorite = favoriteProjectIds.contains(project.id),
                    onFavoriteClick = { onFavoriteClick(project.id) }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(
    name: String,
    role: String,
    description: String,
    skills: List<String>,
    isAvailable: Boolean,
    email: String,
    location: String,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        ProfileCardContent(
            name = name,
            role = role,
            description = description,
            skills = skills,
            isAvailable = isAvailable,
            email = email,
            location = location,
            onContactClick = onContactClick
        )
    }
}

@Composable
fun ProfileCardContent(
    name: String,
    role: String,
    description: String,
    skills: List<String>,
    isAvailable: Boolean,
    email: String,
    location: String,
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContact by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        ProfileHeader(
            name = name,
            role = role,
            isAvailable = isAvailable
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Compétences :",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        skills.forEach { skill ->
            SkillItem(
                skill = skill,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        TextButton(
            onClick = { showContact = !showContact },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = if (showContact) {
                    "Masquer les informations de contact"
                } else {
                    "Afficher les informations de contact"
                }
            )
        }
        if (showContact) {
            ContactField(
                label = "Email",
                value = email,
                icon = Icons.Default.Email,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            ContactField(
                label = "Localisation",
                value = location,
                icon = Icons.Default.LocationOn,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
        ContactButton(
            onContactClick = onContactClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun ProfileAvatar(
    name: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = android.R.drawable.ic_menu_gallery),
            contentDescription = "Avatar de $name",
            modifier = Modifier
                .size(80.dp)
        )
        Availability(
            isAvailable = isAvailable,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun ProfileHeader(
    name: String,
    role: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        ProfileAvatar(
            name = name,
            isAvailable = isAvailable
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = role,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SkillItem(
    skill: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "• $skill",
        modifier = modifier
    )
}

@Composable
fun Availability(
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = if (isAvailable) {
                "Disponible"
            } else {
                "Indisponible"
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun ContactField(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(text = label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null)
        },
        modifier = modifier
    )
}

@Composable
fun ContactButton(
    onContactClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onContactClick,
        modifier = modifier
    ) {
        Text(text = "Me contacter")
    }
}

@Composable
fun ProjectCard(
    project: ProjectUi,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = project.technologies.joinToString(" · "),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ProjectSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(text = "Rechercher un projet ou une techno") },
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        singleLine = true,
        modifier = modifier
    )
}

@Preview(
    showBackground = true,
    name = "Profil - Light Theme"
)
@Composable
fun ProfileScreenLightPreview() {
    ProfilDeveloppeurTheme(darkTheme = false) {
        ProfileScreen(
            projects = sampleProjects(),
            searchQuery = "",
            onSearchQueryChange = {},
            showOnlyFavorites = false,
            onToggleFavoriteFilter = {},
            favoriteProjectIds = emptyList(),
            onFavoriteClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Profil - Dark Theme"
)
@Composable
fun ProfileScreenDarkPreview() {
    ProfilDeveloppeurTheme(darkTheme = true) {
        ProfileScreen(
            projects = sampleProjects().filter { it.id in listOf(3, 4) },
            searchQuery = "Kotlin",
            onSearchQueryChange = {},
            showOnlyFavorites = true,
            onToggleFavoriteFilter = {},
            favoriteProjectIds = listOf(3, 4),
            onFavoriteClick = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileCardUnavailablePreview() {
    ProfilDeveloppeurTheme {
        ProfileCard(
            name = "Thomas M.",
            role = "Développeur Java",
            description = "Développeur Java",
            skills = listOf("Java", "Spring", "Nuxeo"),
            isAvailable = false,
            email = "thomas.m@email.com",
            location = "Paris, France",
            onContactClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
    }
}