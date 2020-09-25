import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;

import java.io.IOException;
import java.util.Arrays;

public class Spotify
{
    String clientId = Config.clientId;
    String clientSecret = Config.clientSecret;
    SpotifyApi spotifyApi;
    String accessToken;
    Spotify()
    {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
        ClientCredentials clientCredentials = null;
        try {
            clientCredentials = clientCredentialsRequest.execute();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SpotifyWebApiException e)
        {
            e.printStackTrace();
        }
        accessToken = clientCredentials.getAccessToken();
        spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();
    }

    public AlbumSimplified[] getArtistAlbums(String artistID, String type)
    {
        GetArtistsAlbumsRequest getArtistsAlbumsRequest = spotifyApi.getArtistsAlbums(artistID)
                .album_type(type)
                .limit(50)
                .offset(0)
                .market(CountryCode.US)
                .build();
        try {
            Paging<AlbumSimplified> albumSimplifiedPaging = getArtistsAlbumsRequest.execute();
            AlbumSimplified[] albums = albumSimplifiedPaging.getItems();
            Arrays.sort(albums, new java.util.Comparator<AlbumSimplified>() {
                @Override
                public int compare(AlbumSimplified o1, AlbumSimplified o2) {
                    return Integer.compare(o1.getName().length(), o2.getName().length());
                }
            });
            return albums;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public TrackSimplified[] getSongsOfAnAlbum(String albumID)
    {
        GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(albumID)
                .limit(50)
                .offset(0)
                .market(CountryCode.SE)
                .build();;
        try {
            Paging<TrackSimplified> trackSimplifiedPaging = getAlbumsTracksRequest.execute();
            return trackSimplifiedPaging.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Artist[] searchArtist(String ArtistName)
    {
        SearchArtistsRequest searchArtistsRequest = spotifyApi.searchArtists(ArtistName)
                .market(CountryCode.SE)
                .limit(50)
                .offset(0)
                .build();
        try {
            Paging<Artist> artistPaging = searchArtistsRequest.execute();
            return artistPaging.getItems();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
