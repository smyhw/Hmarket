package cat.nyaa.hmarket.config;

import cat.nyaa.hmarket.Hmarket;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class HMConfig extends PluginConfigure {
    private final Hmarket plugin;

    @Serializable
    public
    String language = LanguageRepository.DEFAULT_LANGUAGE;
    /*
    tax: # tax for each transaction on every platform, in percent
      market: 10
      signshop: 5
      escrow: 5
      auction: 20
      request: 5
     */
    @Serializable(name = "tax.market")
    public double taxMarket = 10;
    @Serializable(name = "tax.signshop")
    public double taxSignshop = 5;
    @Serializable(name = "tax.escrow")
    public double taxEscrow = 5;
    @Serializable(name = "tax.auction")
    public double taxAuction = 20;
    @Serializable(name = "tax.request")
    public double taxRequest = 5;
    /*
fee: # one time fees for each trade platform
  market: 100
  signshop: 0
  escrow: 50
  auction: 100
  request: 50
     */
    @Serializable(name = "fee.market")
    public double feeMarket = 100;
    @Serializable(name = "fee.signshop")
    public double feeSignshop = 0;
    @Serializable(name = "fee.escrow")
    public double feeEscrow = 50;
    @Serializable(name = "fee.auction")
    public double feeAuction = 100;
    @Serializable(name = "fee.request")
    public double feeRequest = 50;
/*
storage: # storage fee for each trade platform, with free days before start to charge storage fee
  market:
    freedays: 1 # free for 1 day, after 24 hours start to charge storage fee
    base: 0 # fixed amount to charge per itemstack
    percent: 10 # based on unit price per itemstack. E.g., one itemstack cost 100, after 24 hours (1 free day) on the global market, it charges seller 10 every day until sold out or seller take it off
  signshop:
    freedays: -1 # unlimited
    base: 0
    percent: 0
  escrow:
    freedays: 1
    base: 0
    percent: 10
  # auction does not require temp storage
  request:
    freedays: 7
    base: 1
    percent: 0
 */
    @Serializable(name = "storage.market.freedays")
    public int storageMarketFreedays = 1;
    @Serializable(name = "storage.market.base")
    public double storageMarketBase = 0;
    @Serializable(name = "storage.market.percent")
    public double storageMarketPercent = 10;
    @Serializable(name = "storage.signshop.freedays")
    public int storageSignshopFreedays = -1;
    @Serializable(name = "storage.signshop.base")
    public double storageSignshopBase = 0;
    @Serializable(name = "storage.signshop.percent")
    public double storageSignshopPercent = 0;
    @Serializable(name = "storage.escrow.freedays")
    public int storageEscrowFreedays = 1;
    @Serializable(name = "storage.escrow.base")
    public double storageEscrowBase = 0;
    @Serializable(name = "storage.escrow.percent")
    public double storageEscrowPercent = 10;
    @Serializable(name = "storage.request.freedays")
    public int storageRequestFreedays = 7;
    @Serializable(name = "storage.request.base")
    public double storageRequestBase = 1;
    @Serializable(name = "storage.request.percent")
    public double storageRequestPercent = 0;
    /*
limits: # how many resource can a player use?
  slots: #
    market: 5
    signshop-sell: 128
    signshop-buy: 256
  signs: 3
  frames: 12
     */
    @Serializable(name = "limits.slots.market")
    public int limitSlotsMarket = 5;
    @Serializable(name = "limits.slots.signshop-sell")
    public int limitSlotsSignshopSell = 128;
    @Serializable(name = "limits.slots.signshop-buy")
    public int limitSlotsSignshopBuy = 256;
    @Serializable(name = "limits.signs")
    public int limitSigns = 3;
    @Serializable(name = "limits.frames")
    public int limitFrames = 12;
    /*
timers:
  auction:
    duration: 2400 # in ticks, how long does an auction keep?
    interval: 60 # in ticks, how frequent to broadcast an ongoing auction?
  request:
    duration: 2400
    interval: 60
     */
    @Serializable(name = "timers.auction.duration")
    public int timerAuctionDuration = 2400;
    @Serializable(name = "timers.auction.interval")
    public int timerAuctionInterval = 60;
    @Serializable(name = "timers.request.duration")
    public int timerRequestDuration = 2400;
    @Serializable(name = "timers.request.interval")
    public int timerRequestInterval = 60;

    public HMConfig(Hmarket plugin) {
        this.plugin = plugin;
        load();
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }
}
