package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.block.CircuitSwitchBlock;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;

public class CircuitSwitchBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {

    private boolean powered;

    public CircuitSwitchBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.CIRCUIT_SWITCH, pos, blockState);
        this.powered = true;

        if(blockState.hasProperty(CircuitSwitchBlock.POWERED)) {
            this.setPowered(blockState.getValue(CircuitSwitchBlock.POWERED));
        } else {
            this.setPowered(false);
        }
    }

    public void setPowered(boolean powered) {
        if(this.powered != powered) {
            this.powered = powered;

            CircuitNode startNode = this.wire.getStartNode();
            CircuitNode endNode = this.wire.getEndNode();
            if(!powered) {
                if(startNode.getNetwork() != null) {
                    startNode.getNetwork().removeWire(this.wire);
                }
                if(endNode.getNetwork() != null && endNode.getNetwork() != startNode.getNetwork()) {
                    endNode.getNetwork().removeWire(this.wire);
                }
                this.miniCircuit.removeInternalWire(this.wire);
            } else {
                this.miniCircuit.addInternalWire(this.wire);
                if(startNode.getNetwork() == null) {
                    startNode.setNetwork(new CircuitNetwork());
                }
                if(endNode.getNetwork() == null) {
                    endNode.setNetwork(new CircuitNetwork());
                }
                if(startNode.getNetwork() != endNode.getNetwork()) {
                    CircuitNetwork network = startNode.getNetwork();
                    network.merge(endNode.getNetwork());
                    endNode.setNetwork(network);
                }

                startNode.getNetwork().addWire(this.wire);
            }
        }
    }
}
