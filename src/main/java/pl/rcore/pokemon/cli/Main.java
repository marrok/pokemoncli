package pl.rcore.pokemon.cli;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.NoSuchItemException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Created by mariusz on 2016-09-29.
 */
public class Main {

    private static String TOKEN = "4/P68KW3u8TyOfJvT5XGNiitvRHzHFPdQj2pSTRjvTuG8";
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String args[]) throws LoginFailedException, RemoteServerException {

        OkHttpClient httpClient = new OkHttpClient();
        GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient);

        System.out.println("Please go to " + GoogleUserCredentialProvider.LOGIN_URL);
        System.out.println("Enter authorization code:");

        Scanner sc = new Scanner(System.in);
        String access = sc.nextLine();

        provider.login(access);
        String refreshToken = provider.getRefreshToken();

        PokemonGo go = new PokemonGo(httpClient);
        go.login(provider);

        double lat = 52.249789334811;
        double lng = 21.014088392257694;


        go.setLocation(lat, lng, 1);
        List<CatchablePokemon> pokemons = go.getMap().getCatchablePokemon();

        for(CatchablePokemon p : pokemons)
        {
            try {
                EncounterResult e =  p.encounterNormalPokemon();

                System.out.println("---------------------------------------------------");
                System.out.println(p.getPokemonId().name());
                System.out.println(e.getPokemonData().getCp());


                CatchResult result = p.catchPokemon();

                System.out.println(result.getStatus());

            } catch (NoSuchItemException e) {

            }

        }



        logger.info("test");
    }
}
