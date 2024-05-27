import net.glenmazza.splashclient.model.Item
import net.glenmazza.splashclient.model.Meta

class SingleItemResponse<T : Item?> {
    var meta: Meta? = null
    var data: T? = null
    var success = false
}
