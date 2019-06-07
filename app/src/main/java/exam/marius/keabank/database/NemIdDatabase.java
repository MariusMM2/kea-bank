package exam.marius.keabank.database;

import android.content.Context;
import exam.marius.keabank.model.NemId;

class NemIdDatabase extends AbstractDatabase<NemId> {
    NemIdDatabase(Context context) {
        super(context);
    }

    @Override
    String getItemsFileName() {
        return "nemids";
    }
}
