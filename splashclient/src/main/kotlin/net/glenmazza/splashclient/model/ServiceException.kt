package net.glenmazza.splashclient.model

class ServiceException(message: String?, val statusCode: Int) :
    RuntimeException(String.format("Status Code %d: %s", statusCode, message)) {

    companion object {
        private const val serialVersionUID = -7661881974219233311L
    }
}
