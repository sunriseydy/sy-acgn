package anime.tools.torrent;

import java.util.List;

public record TorrentFile(Long fileLength, List<String> fileDirs) {

    @Override
    public String toString() {
        return "TorrentFile{" +
                "fileLength=" + fileLength +
                ", fileDirs=" + fileDirs +
                '}';
    }

    /// /////////////////////////////////////////////////////////////////////////
    /// / GETTERS AND SETTERS ///////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////////////////////////
    @Override
    public Long fileLength() {
        return fileLength;
    }
}
