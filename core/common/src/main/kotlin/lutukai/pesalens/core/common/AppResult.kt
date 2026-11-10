package lutukai.pesalens.core.common

sealed interface AppError {
    data object NotConnected : AppError

    data object Storage : AppError

    data object Network : AppError

    data object InvalidData : AppError
}

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>
}
