package pl.rcore.pokemon.cli;

import POGOProtos.Data.PokemonDataOuterClass;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Pokeball;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.api.settings.CatchOptions;
import com.pokegoapi.auth.CredentialProvider;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.exceptions.LoginFailedException;
import com.pokegoapi.exceptions.NoSuchItemException;
import com.pokegoapi.exceptions.RemoteServerException;
import okhttp3.OkHttpClient;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by mariusz on 2016-09-29.
 */
public class Main {

    private static String TOKEN = "4/P68KW3u8TyOfJvT5XGNiitvRHzHFPdQj2pSTRjvTuG8";
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String args[]) throws LoginFailedException, RemoteServerException, NoSuchItemException {

        OkHttpClient httpClient = new OkHttpClient();
        GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(httpClient);

        System.out.println("Please go to " + GoogleUserCredentialProvider.LOGIN_URL);
        System.out.println("Enter authorization code:");

        Scanner sc = new Scanner(System.in);
        String access = sc.nextLine();

        provider.login(access);

        PokemonGo go = new PokemonGo(httpClient);
        go.login(provider);

        String line = "";

        while(!"exit".equals(line))
        {
            try
            {
                System.out.print("Enter command: goto, ls, exit, catch, transfer: ");
                line = sc.nextLine();
                if("goto".equalsIgnoreCase(line))
                    goToLocation(go);
                else if("transfer".equalsIgnoreCase(line))
                    transferPokemons(go);
                else if("ls".equalsIgnoreCase(line))
                    listPokemons(go);
                else if("catch".equalsIgnoreCase(line))
                    catchPokemon(go);
                else if("exit".equalsIgnoreCase(line))
                    break;
            }
            catch(Exception e)
            {
                System.out.println("Exception: " + e.getMessage());
            }

        }
    }

    private static void catchPokemon(PokemonGo go) throws LoginFailedException, RemoteServerException, NoSuchItemException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Pokemon name: ");
        String name = scanner.nextLine();

        Optional<CatchablePokemon> p = go.getMap().getCatchablePokemon().stream().filter(x-> name.equalsIgnoreCase(x.getPokemonId().name())).findFirst();

        if(p.isPresent())
        {
            EncounterResult e = p.get().encounterNormalPokemon();

            if(e.wasSuccessful())
            {
                CatchOptions options = new CatchOptions(go);

                System.out.print("Enter pokeball type n|g|u|default: ");
                String pokeball = scanner.nextLine();
                if("n".equalsIgnoreCase(pokeball))
                    options.usePokeball(Pokeball.POKEBALL);
                else if("g".equalsIgnoreCase(pokeball))
                    options.usePokeball(Pokeball.GREATBALL);
                else if("u".equalsIgnoreCase(pokeball))
                    options.usePokeball(Pokeball.ULTRABALL);

                System.out.print("Enter user berries y|n: ");
                String berries = scanner.nextLine();
                if("y".equalsIgnoreCase(berries))
                    options.useRazzberries(true);

                p.get().encounterNormalPokemon().getPokemonData().getCp();
                CatchResult result = p.get().catchPokemon(options);
                System.out.println(result.getStatus());
            }
            else
            {
                System.out.println("Encounter was not successfull");
            }
        }
        else
        {
            System.out.println("There is no Pokemon with name " + name);
        }
    }

    private static void transferPokemons(PokemonGo go) throws LoginFailedException, RemoteServerException {
        List<Pokemon> ps = go.getInventories().getPokebank().getPokemons().stream()
                                .sorted((x, y) -> x.getPokemonId().name().compareTo(y.getPokemonId().name()))
                                .collect(Collectors.toList());
        for(Pokemon p : ps)
            System.out.println("Pokemon: " + p.getPokemonId().name() + " cp: " + p.getCp());

        System.out.print("Pokemon name: ");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();

        ps = ps.stream().filter(x -> name.equals(x.getPokemonId().name())).collect(Collectors.toList());

        for(Pokemon p : ps)
        {
            System.out.print("Pokemon transfer pokemon " + p.getPokemonId().name() + " cp: " + p.getCp() + " y|n");
            String transfer = scanner.nextLine();
            if ("y".equalsIgnoreCase(transfer))
                p.transferPokemon();
        }
    }

    private static void listPokemons(PokemonGo go) throws LoginFailedException, RemoteServerException
    {
        List<CatchablePokemon> pokemons = go.getMap().getCatchablePokemon();
        for(CatchablePokemon p : pokemons)
        {
            PokemonDataOuterClass.PokemonData pd =  p.encounterNormalPokemon().getPokemonData();
            System.out.println("Pokemon: " + pd.getPokemonId().name() + " cp: " + pd.getCp());
        }
    }

    private static void goToLocation(PokemonGo go)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter lat,lng: ");
        String[] latlng = scanner.nextLine().split(",");

        double lat = Double.parseDouble(latlng[0].replace(",", "."));
        double lng = Double.parseDouble(latlng[1].replace(",", "."));
        go.setLocation(lat, lng, 1);
    }
}
