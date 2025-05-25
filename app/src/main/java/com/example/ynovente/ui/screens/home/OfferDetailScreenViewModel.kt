import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ynovente.data.model.Offer
import com.example.ynovente.data.repository.FirebaseOfferRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class OfferDetailViewModel(
    repo: FirebaseOfferRepository,
    offerId: String
) : ViewModel() {
    val offer: StateFlow<Offer?> = repo.getOfferByIdFlow(offerId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}