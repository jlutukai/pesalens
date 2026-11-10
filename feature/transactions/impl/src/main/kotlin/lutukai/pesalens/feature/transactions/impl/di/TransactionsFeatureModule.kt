package lutukai.pesalens.feature.transactions.impl.di

import lutukai.pesalens.core.navigation.PesaPanes
import lutukai.pesalens.feature.transactions.api.CategoryTransactions
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsScreen
import lutukai.pesalens.feature.transactions.impl.presentation.TransactionsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
val transactionsFeatureModule = module {
    viewModelOf(::TransactionsViewModel)

    navigation<CategoryTransactions>(metadata = PesaPanes.detail()) { TransactionsScreen(it) }
}
