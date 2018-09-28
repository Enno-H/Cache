package FileFragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileFragments implements Serializable {
    private List<String> fragmentDigestList = new ArrayList<>();

    public List<String> getFragmentDigestList() {
        return fragmentDigestList;
    }

    public void setFragmentDigestList(List<String> fragmentDigestList) {
        this.fragmentDigestList = fragmentDigestList;
    }
}
